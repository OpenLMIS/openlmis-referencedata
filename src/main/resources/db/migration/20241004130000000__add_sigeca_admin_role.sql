INSERT INTO roles (id, description, name)
VALUES ('cacf7901-02c7-4452-bc50-5afa4c2d5275',
        'SIGECA SynchronizationAdministrator, i.e. receives synchronization summary emails',
        'SIGECA Synchronization Admin');

INSERT INTO rights (id, name, type)
VALUES ('08f53a3e-54fd-43f9-af5b-74029974ebe9', 'SIGECA_SYNC_MANAGE', 'GENERAL_ADMIN');

INSERT INTO role_rights (roleid, rightid)
VALUES ('cacf7901-02c7-4452-bc50-5afa4c2d5275', '08f53a3e-54fd-43f9-af5b-74029974ebe9');
