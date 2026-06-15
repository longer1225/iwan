UPDATE sys_user 
SET doc = jsonb_set(doc, '{password}', '"$2b$12$yDjSXX7Qq7Ryipu6FndFZe94V9QlySRa0ThBwJBEMQhEO266VJEBW"') 
WHERE doc->>'username' = 'iwanna';

UPDATE sys_user 
SET doc = jsonb_set(doc, '{password}', '"$2b$12$yDjSXX7Qq7Ryipu6FndFZe94V9QlySRa0ThBwJBEMQhEO266VJEBW"') 
WHERE doc->>'username' = 'iwanna2';
