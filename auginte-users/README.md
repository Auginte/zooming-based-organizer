Users service for Augitne
=========================

Problems/goals
--------------

* I wanted to group my existing repositories
 * So, user table with links to repositories
* I want repository to be isolated and distributed
 * So, repository service should have database users created
 * So, session id should be used to authenticate (ip, password, routing to server)
* I want demo page to be accessible without registration
 * So, IP/hash based repository is needed
 * Creating new database on save (optimise with precreated databases)
 
Authentication/session management
---------------------------------

* E-mail is used as primary identifier
 * Hash e-mail to find database among servers
* Use database per user
 * User via login have database user `owner`
 * User via session will have database user name `s_` + hash + expiration
 * Password recovery database user will have name `recovery`
* When logged in with session or recovery database user
 * Login logic will recheck `expiration date`/`ip`/`user agent` details and disable database user, if needed
 * User details will hold `ip.created`, `date.created`, `ip.lastUsed`, `date.lastUsed`
* User service will have permission to:
 * Create New database with `owner` user
 * Create/update `recovery` user in database (only one active recovery hash)
* When user is logged in:
 * It can change `owner` password
  * System should be resilient to allow free change of database internals
 * Create session users with `expiration date`/`ip`/`user agent` details
* External authentications will hold extra fields, which will be used in login logic
 * Passwords are hashed to prevent full access to database
 * External access users:
  * `p_` + hash + camera: Read only public access. Fields: `camera`, `expirationDate`
  * `c_` + hash + camera: Contributor with write access. Fields: `camera`, `name`, `date.created`, `date.lastUsed`
  * `e_` + type + token: External logins like Google, Facebook, Twitter. Fields `type`, `token`, `date.created`, `date.lastUsed` and other
