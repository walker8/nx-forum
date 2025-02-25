#!/bin/bash

# NX Forum 一键打包脚本
# 功能：打包后端服务（jar包）和前端服务（zip包）到 output 目录

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 获取脚本所在目录的父目录（项目根目录）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
OUTPUT_DIR="$PROJECT_ROOT/output"
BACKEND_DIR="$PROJECT_ROOT/nx-forum-backend"
FRONTEND_DIR="$PROJECT_ROOT/nx-forum-nuxt"
BACKEND_START_DIR="$BACKEND_DIR/nx-biz-forum/nx-forum-start"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  NX Forum 一键打包脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 清理 output 目录
echo -e "${YELLOW}[1/5] 清理输出目录...${NC}"
if [ -d "$OUTPUT_DIR" ]; then
    rm -rf "$OUTPUT_DIR"/*
    echo -e "${GREEN}✓ 输出目录已清理${NC}"
else
    mkdir -p "$OUTPUT_DIR"
    echo -e "${GREEN}✓ 输出目录已创建${NC}"
fi
echo ""

# 检查后端目录是否存在
if [ ! -d "$BACKEND_DIR" ]; then
    echo -e "${RED}✗ 错误：后端目录不存在: $BACKEND_DIR${NC}"
    exit 1
fi

# 检查前端目录是否存在
if [ ! -d "$FRONTEND_DIR" ]; then
    echo -e "${RED}✗ 错误：前端目录不存在: $FRONTEND_DIR${NC}"
    exit 1
fi

# 打包后端
echo -e "${YELLOW}[2/5] 开始打包后端服务...${NC}"
cd "$BACKEND_DIR"

# 检查 Maven 是否安装
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}✗ 错误：未找到 Maven，请先安装 Maven${NC}"
    exit 1
fi

echo "执行: mvn clean package -DskipTests"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}✗ 后端打包失败${NC}"
    exit 1
fi

# 查找并复制 jar 包
JAR_FILE="$BACKEND_START_DIR/target/nx-forum-start.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}✗ 错误：未找到 jar 包: $JAR_FILE${NC}"
    exit 1
fi

cp "$JAR_FILE" "$OUTPUT_DIR/nx-forum-start.jar"
echo -e "${GREEN}✓ 后端打包完成: nx-forum-start.jar${NC}"
echo ""

# 打包前端
echo -e "${YELLOW}[3/5] 开始构建前端服务...${NC}"
cd "$FRONTEND_DIR"

# 检查 Node.js 是否安装
if ! command -v node &> /dev/null; then
    echo -e "${RED}✗ 错误：未找到 Node.js，请先安装 Node.js${NC}"
    exit 1
fi

# 检查 yarn 是否安装
if ! command -v yarn &> /dev/null; then
    echo -e "${RED}✗ 错误：未找到 Yarn，请先安装 Yarn${NC}"
    exit 1
fi

# 安装依赖（如果需要）
if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    yarn install
fi

# 构建前端
echo "执行: yarn build"
yarn build

if [ $? -ne 0 ]; then
    echo -e "${RED}✗ 前端构建失败${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 前端构建完成${NC}"
echo ""

# 打包前端为 zip
echo -e "${YELLOW}[4/5] 开始打包前端为 zip...${NC}"

# 检查 .output 目录是否存在
if [ ! -d "$FRONTEND_DIR/.output" ]; then
    echo -e "${RED}✗ 错误：未找到 .output 目录${NC}"
    exit 1
fi

# 进入 .output 目录并打包
cd "$FRONTEND_DIR/.output"

# 创建 zip 包，排除 uploads 目录
ZIP_FILE="$OUTPUT_DIR/nx-forum-nuxt.zip"
zip -r "$ZIP_FILE" . -x "public/uploads/*" -q

if [ $? -ne 0 ]; then
    echo -e "${RED}✗ 前端打包失败${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 前端打包完成: nx-forum-nuxt.zip${NC}"
echo ""

# 显示打包结果
echo -e "${YELLOW}[5/5] 打包完成！${NC}"
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  打包结果${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "输出目录: $OUTPUT_DIR"
echo ""
ls -lh "$OUTPUT_DIR" | grep -E "\.(jar|zip)$" | awk '{print "  " $9 " (" $5 ")"}'
echo ""
echo -e "${GREEN}✓ 所有打包任务已完成！${NC}"

