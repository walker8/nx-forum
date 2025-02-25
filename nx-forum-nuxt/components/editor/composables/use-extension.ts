import {
  type AnyExtension,
  type Extensions,
} from "@tiptap/core";

export function useExtension() {
  const filterDuplicateExtensions = (extensions: Extensions | undefined) => {
    if (!extensions) {
      return;
    }
    const resolvedExtensions = sort(extensions);
    const map = new Map<string, AnyExtension>();
    resolvedExtensions.forEach((extension) => {
      if (!extension.name) {
        console.warn(
          `Extension name is missing for Extension, type: ${extension.type}.`
        );
        // Generate a random key if name is missing (simplified UUID)
        const key = Math.random().toString(36).substring(2, 15);
        map.set(key, extension);
        return;
      }
      const key = `${extension.type}-${extension.name}`;
      if (map.has(key)) {
        console.warn(
          `Duplicate found for Extension, type: ${extension.type}, name: ${extension.name}. Keeping the later one.`
        );
      }
      map.set(key, extension);
    });
    return Array.from(map.values());
  };

  /**
   * Sort extensions by priority.
   * @param extensions An array of Tiptap extensions
   * @returns A sorted array of Tiptap extensions by priority
   */
  const sort = (extensions: Extensions): Extensions => {
    const defaultPriority = 100;

    return extensions.sort((a, b) => {
      const priorityA = (a.options as any).priority || defaultPriority;
      const priorityB = (b.options as any).priority || defaultPriority;

      if (priorityA > priorityB) {
        return -1;
      }

      if (priorityA < priorityB) {
        return 1;
      }

      return 0;
    });
  };

  return {
    filterDuplicateExtensions,
  };
}
