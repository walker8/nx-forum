#!/usr/bin/env python3
"""
MySQL Flyway Migration Script to H2 Converter

This script converts MySQL-specific Flyway migration scripts to H2-compatible format.
It handles:
- Removing MySQL-specific comments and settings
- Converting data types (longtext, mediumtext -> clob)
- Removing unsigned, character set, collate, engine specifications
- Removing LOCK TABLES and other MySQL-specific commands
- Preserving string literals in INSERT statements
"""

import re
import sys
import os
from pathlib import Path


def convert_mysql_to_h2(mysql_sql_content):
    """Convert MySQL SQL to H2 compatible SQL."""

    # First pass: handle data type conversions in CREATE TABLE only
    lines = mysql_sql_content.split('\n')
    h2_lines = []
    in_create_table = False
    current_statement = []

    for line in lines:
        # Skip MySQL-specific version comments and settings
        if line.strip().startswith('/*!'):
            continue

        # Skip unwanted commands
        if any(cmd in line.upper() for cmd in ['LOCK TABLES', 'UNLOCK TABLES',
                                                'DISABLE KEYS', 'ENABLE KEYS',
                                                'DROP TABLE IF EXISTS']):
            continue

        # Skip dump headers
        if '-- Dump completed' in line or '-- MySQL dump' in line:
            continue

        # Detect CREATE TABLE
        if re.match(r'^\s*CREATE\s+TABLE', line, re.IGNORECASE):
            in_create_table = True
            current_statement = [line]
            continue

        if in_create_table:
            current_statement.append(line)

            # Check if we've reached the end of CREATE TABLE
            if ';' in line:
                # Process the complete CREATE TABLE
                create_table = '\n'.join(current_statement)
                result = process_create_table(create_table)
                h2_lines.append(result)
                in_create_table = False
                current_statement = []
        else:
            # Process other statements (INSERT, etc)
            processed = process_non_create_line(line)
            if processed:
                h2_lines.append(processed)

    return '\n'.join(h2_lines)


def process_create_table(table_sql):
    """Process a CREATE TABLE statement and convert to H2 format."""

    # Remove backticks
    table_sql = table_sql.replace('`', '')

    # Fix index names: remove spaces within index names (e.g., "uc_user_logs_ create_time_IDX" -> "uc_user_logs_create_time_IDX")
    # Pattern matches: KEY name_with_space (column) or KEY prefix suffix (column)
    table_sql = re.sub(r'KEY\s+([\w_]+)\s+([\w_]+)\s*\(', r'KEY \1\2 (', table_sql)

    # Remove unsigned keyword
    table_sql = re.sub(r'\bunsigned\b', '', table_sql, flags=re.IGNORECASE)

    # Remove CHARACTER SET and COLLATE clauses (everywhere in the statement)
    table_sql = re.sub(r'\s+CHARACTER\s+SET\s+[\w\d]+', '', table_sql, flags=re.IGNORECASE)
    table_sql = re.sub(r'\s+CHARACTER\s+set\s+[\w\d]+', '', table_sql, flags=re.IGNORECASE)
    # COLLATE is usually followed by charset name like utf8mb4_general_ci
    # Match only the COLLATE clause itself, not subsequent keywords
    table_sql = re.sub(r'\s+COLLATE\s+[\w\d_]+', '', table_sql, flags=re.IGNORECASE)

    # Remove ROW_FORMAT
    table_sql = re.sub(r'\s+ROW_FORMAT=\w+', '', table_sql, flags=re.IGNORECASE)

    # Convert text types to clob - only in column definitions (not in string literals)
    # Match patterns like: longtext, mediumtext, text (but not inside quotes)
    def replace_text_type(match):
        """Replace data type text with clob, preserving comments."""
        word = match.group(0)
        # Only replace if it's a data type (followed by space, comma, or paren)
        return 'clob'

    # Use word boundaries to avoid replacing 'text' in string content
    table_sql = re.sub(r'\blongtext\b', 'clob', table_sql, flags=re.IGNORECASE)
    table_sql = re.sub(r'\bmediumtext\b', 'clob', table_sql, flags=re.IGNORECASE)
    # Be more careful with 'text' - only replace when it's clearly a type
    # Pattern: text followed by space, NOT NULL, comma, or closing paren
    table_sql = re.sub(r'\btext\b(?=\s*(?:NOT NULL|NULL|DEFAULT|,|\)|CHARACTER|COLLATE|$))',
                     'clob', table_sql, flags=re.IGNORECASE)

    # Remove USING BTREE from index definitions
    table_sql = re.sub(r'\s+USING\s+BTREE', '', table_sql, flags=re.IGNORECASE)

    # Remove AUTO_INCREMENT if present (H2 handles this differently)
    table_sql = re.sub(r'\s+AUTO_INCREMENT=\d+', '', table_sql, flags=re.IGNORECASE)

    # Handle ENGINE= clause (remove all of it, H2 doesn't support table-level COMMENT)
    # Match: ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='xxx'
    # Use non-greedy matching
    engine_full_pattern = r'\)\s*ENGINE=\w+.*?(?:COMMENT=\'[^\']*\'\s*)?;'

    def replace_engine(match):
        return ');'

    table_sql = re.sub(engine_full_pattern, replace_engine, table_sql, flags=re.IGNORECASE)

    # Final cleanup: remove any remaining MySQL-specific clauses before closing paren
    # This handles cases where COLLATE or ROW_FORMAT appear after ENGINE clause
    # Match: ) COLLATE=xxx COMMENT='yyy' -> ) COMMENT='yyy'
    table_sql = re.sub(r'\)\s+COLLATE\s*=\s*[\w\d_]+\s*(?=COMMENT)', '', table_sql, flags=re.IGNORECASE)
    table_sql = re.sub(r'\)\s+ROW_FORMAT\s*=\s*\w+\s*(?=COMMENT)', '', table_sql, flags=re.IGNORECASE)
    table_sql = re.sub(r'\)\s+DEFAULT\s+CHARSET\s*=\s*[\w\d]+\s*(?=COMMENT)', '', table_sql, flags=re.IGNORECASE)
    # Also handle cases where these appear without COMMENT (at the very end)
    table_sql = re.sub(r'\)\s+COLLATE\s*=\s*[\w\d_]+\s*(?=\s*;)', ')', table_sql, flags=re.IGNORECASE)
    table_sql = re.sub(r'\)\s+ROW_FORMAT\s*=\s*\w+\s*(?=\s*;)', ')', table_sql, flags=re.IGNORECASE)
    table_sql = re.sub(r'\)\s+DEFAULT\s+CHARSET\s*=\s*[\w\d]+\s*(?=\s*;)', ')', table_sql, flags=re.IGNORECASE)

    # Clean up multiple spaces
    table_sql = re.sub(r'  +', ' ', table_sql)

    # Remove trailing comma before closing paren
    table_sql = re.sub(r',\s*\)', '\n)', table_sql)

    # Remove double commas
    table_sql = re.sub(r',\s*,', ',', table_sql)

    # Format: add proper newlines for readability
    table_sql = re.sub(r'\)\s*;', '\n);', table_sql)

    # Remove table-level COMMENT at the end (H2 doesn't support ) COMMENT='xxx' syntax)
    table_sql = re.sub(r'\)\s+COMMENT=\'[^\']*\'\s*;', ');', table_sql, flags=re.IGNORECASE)

    return table_sql


def process_insert_statement(insert_sql):
    """Process INSERT statement to handle H2 compatibility."""
    # Remove backticks
    insert_sql = insert_sql.replace('`', '')

    # Replace MySQL NOW() with H2 CURRENT_TIMESTAMP
    insert_sql = re.sub(r'\bNOW\(\)', 'CURRENT_TIMESTAMP', insert_sql, flags=re.IGNORECASE)

    # Handle string escaping for H2
    # MySQL uses \" to escape double quotes within strings
    # H2 in standard SQL mode doesn't need this escaping if the outer string uses single quotes
    # So we convert \" to just " (remove the backslash escape)
    values_match = re.search(r'VALUES\s+(.*)', insert_sql, re.IGNORECASE)
    if values_match:
        values_part = values_match.group(1)
        # Remove MySQL-style escaping for double quotes: \" -> "
        # Keep the JSON double quotes intact, just remove the backslash escapes
        values_part = values_part.replace(r'\"', '"')
        # Also handle escaped single quotes: \' -> '' (H2 standard)
        values_part = values_part.replace(r"\'", "''")
        # Reconstruct the statement
        insert_sql = re.sub(r'VALUES\s+.*', 'VALUES ' + values_part, insert_sql, flags=re.IGNORECASE)

    return insert_sql.strip()


def process_non_create_line(line):
    """Process a single SQL line (outside CREATE TABLE)."""

    # Remove backticks
    line = line.replace('`', '')

    # Skip empty lines
    if not line.strip():
        return None

    # Keep comment lines
    if line.strip().startswith('--'):
        return line.strip()

    # Handle INSERT statements specially
    if re.match(r'^\s*INSERT\s+', line, re.IGNORECASE):
        return process_insert_statement(line)

    # For other SQL statements:
    # Remove unsigned
    line = re.sub(r'\bunsigned\b', '', line, flags=re.IGNORECASE)

    # Remove CHARACTER SET and COLLATE
    line = re.sub(r'\s+CHARACTER\s+SET\s+\w+', '', line, flags=re.IGNORECASE)
    line = re.sub(r'\s+COLLATE\s+\w+', '', line, flags=re.IGNORECASE)

    # Replace MySQL NOW() with H2 CURRENT_TIMESTAMP
    line = re.sub(r'\bNOW\(\)', 'CURRENT_TIMESTAMP', line, flags=re.IGNORECASE)

    # Clean up multiple spaces (but preserve newlines in multi-line statements)
    line = re.sub(r'  +', ' ', line)

    return line.strip()


def main():
    """Main conversion function."""
    if len(sys.argv) < 2:
        print("Usage: python mysql2h2.py <mysql_sql_file> [output_file]")
        print("\nExample:")
        print("  python mysql2h2.py V1.0.0_250629__init.sql V1.0.0_250629__init_h2.sql")
        sys.exit(1)

    input_file = sys.argv[1]

    if not os.path.exists(input_file):
        print(f"Error: Input file '{input_file}' not found!")
        sys.exit(1)

    # Determine output file
    if len(sys.argv) >= 3:
        output_file = sys.argv[2]
    else:
        # Generate output filename
        input_path = Path(input_file)
        output_file = input_path.parent / f"{input_path.stem}_h2{input_path.suffix}"

    # Read input file
    print(f"Reading from: {input_file}")
    with open(input_file, 'r', encoding='utf-8') as f:
        mysql_sql = f.read()

    # Convert
    print("Converting MySQL to H2...")
    h2_sql = convert_mysql_to_h2(mysql_sql)

    # Write output file
    print(f"Writing to: {output_file}")
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(h2_sql)

    print("\nConversion completed successfully!")
    print(f"\nTo use the converted file:")
    print(f"1. Copy '{output_file}' to your H2 migration directory")
    print(f"2. Ensure it has the correct Flyway version name (e.g., V1.0.0__description.sql)")
    print(f"3. The converted script includes both table definitions and initial data")


if __name__ == '__main__':
    main()
