import bcrypt

# 生成 123456 的 bcrypt 哈希
password = "123456".encode('utf-8')
hashed = bcrypt.hashpw(password, bcrypt.gensalt())
print(f"hash={hashed.decode()}")
