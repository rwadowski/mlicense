CREATE UNIQUE INDEX "users_login_lowercase" ON "users"("login_lowercase");
CREATE UNIQUE INDEX "users_email" ON "users"("email");
CREATE UNIQUE INDEX "application_name" ON "applications"("name");