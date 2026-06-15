# 好友请求功能测试脚本

# 1. 测试搜索用户功能
echo "=== 测试搜索用户功能 ==="
curl -s "http://localhost:8080/iwan/api/v1/search?keyword=iwanna2&type=user"
echo -e "\n"

# 2. 测试登录功能
echo "=== 测试登录功能 ==="
curl -s -X POST "http://localhost:8080/iwan/api/v1/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"iwanna","password":"password123"}'
echo -e "\n"

# 3. 测试发送好友请求（需要先获取token）
echo "=== 测试发送好友请求功能 ==="
echo "注意：需要先登录获取token才能发送好友请求"
echo -e "\n"

# 4. 测试获取好友请求列表
echo "=== 测试获取好友请求列表功能 ==="
echo "注意：需要先登录获取token才能查看好友请求"
echo -e "\n"

# 5. 测试获取好友列表
echo "=== 测试获取好友列表功能 ==="
echo "注意：需要先登录获取token才能查看好友列表"
echo -e "\n"