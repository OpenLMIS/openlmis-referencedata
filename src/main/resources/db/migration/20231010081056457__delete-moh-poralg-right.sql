DELETE FROM referencedata.role_rights WHERE rightid = '0f41c043-d61c-44ad-837d-f7978efc8978';

DELETE FROM referencedata.right_assignments WHERE rightname = 'MOH_PORALG_APPROVALS';

DELETE FROM referencedata.rights WHERE id = '0f41c043-d61c-44ad-837d-f7978efc8978' or name = 'MOH_PORALG_APPROVALS';

