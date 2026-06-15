import bcrypt
import psycopg2

# 生成 123456 的 bcrypt 哈希
password = "123456".encode('utf-8')
hashed = bcrypt.hashpw(password, bcrypt.gensalt())
print(f"Generated hash: {hashed.decode()}")

# 连接数据库并更新密码
conn = psycopg2.connect(
    dbname="iwan_blog",
    user="postgres",
    password="password",
    host="localhost",
    port="5432"
)
cursor = conn.cursor()

# 更新 iwanna 的密码
cursor.execute("""
    UPDATE sys_user 
    SET doc = jsonb_set(doc, '{password}', %s) 
    WHERE doc->>'username' = 'iwanna'
""", (hashed.decode(),))

# 更新 iwanna2 的密码
cursor.execute("""
    UPDATE sys_user 
    SET doc = jsonb_set(doc, '{password}', %s) 
    WHERE doc->>'username' = 'iwanna2'
""", (hashed.decode(),))

conn.commit()
cursor.close()
conn.close()
print("密码更新成功！")
