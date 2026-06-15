UPDATE sys_user SET doc = jsonb_set(doc, '{password}', '"$2b$10$fnEwFEJ4uLblpPwTJ2J.fuu7i3mWbSniZ1OcYCuhtNXteerrgiNZK"') WHERE doc->>'username' IN ('iwanna', 'iwanna2');
