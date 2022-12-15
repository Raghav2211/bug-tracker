print('############################# INIT Script ####################################');
db = db.getSiblingDB('bug-tracker');
db.createCollection('user');
db.createCollection("project");
db.user.createIndex( {email:1}, { unique: true } );
db.project.createIndex( {name:1}, { unique: true } );
print('############################# INIT Script ####################################');