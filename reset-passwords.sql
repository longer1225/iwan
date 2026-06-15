UPDATE sys_user
SET doc = jsonb_set(doc, '{password}', '"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"')
WHERE doc->>'username' = 'iwanna';

UPDATE sys_user
SET doc = jsonb_set(doc, '{password}', '"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"')
WHERE doc->>'username' = 'iwanna2';
