
CREATE SEQUENCE cwf_workflowitem_seq;
CREATE SEQUENCE cwf_collectionrole_seq;
CREATE SEQUENCE cwf_workflowitemrole_seq;
CREATE SEQUENCE cwf_pooltask_seq;
CREATE SEQUENCE cwf_claimtask_seq;
CREATE SEQUENCE cwf_in_progress_user_seq;

CREATE TABLE cwf_workflowitem
(
  workflowitem_id integer DEFAULT nextval('cwf_workflowitem_seq') PRIMARY KEY,
  item_id        INTEGER REFERENCES item(item_id) UNIQUE,
  collection_id  INTEGER REFERENCES collection(collection_id),

  -- Answers to questions on first page of submit UI
  multiple_titles       BOOL,
  published_before      BOOL,
  multiple_files        BOOL
  -- Note: stage reached not applicable here - people involved in workflow
  -- can always jump around submission UI

);

CREATE INDEX cwf_workflowitem_item_fk_idx ON cwf_workflowitem(item_id);
CREATE INDEX cwf_workflowitem_coll_fk_idx ON cwf_workflowitem(collection_id);


CREATE TABLE cwf_collectionrole (
collectionrole_id integer DEFAULT nextval('cwf_collectionrole_seq') PRIMARY KEY,
role_id Text,
collection_id integer REFERENCES collection(collection_id),
group_id integer REFERENCES epersongroup(eperson_group_id)
);
ALTER TABLE ONLY cwf_collectionrole
ADD CONSTRAINT cwf_collectionrole_unique UNIQUE (role_id, collection_id, group_id);

CREATE INDEX cwf_collectionrole_coll_role_fk_idx ON cwf_collectionrole(collection_id,role_id);
CREATE INDEX cwf_collectionrole_coll_fk_idx ON cwf_collectionrole(collection_id);


CREATE TABLE cwf_workflowitemrole (
  workflowitemrole_id integer DEFAULT nextval('cwf_workflowitemrole_seq') PRIMARY KEY,
  role_id Text,
  workflowitem_id integer REFERENCES cwf_workflowitem(workflowitem_id),
  eperson_id integer REFERENCES eperson(eperson_id),
  group_id integer REFERENCES epersongroup(eperson_group_id)
);
ALTER TABLE ONLY cwf_workflowitemrole
ADD CONSTRAINT cwf_workflowitemrole_unique UNIQUE (role_id, workflowitem_id, eperson_id);

CREATE INDEX cwf_workflowitemrole_item_role_fk_idx ON cwf_workflowitemrole(workflowitem_id,role_id);
CREATE INDEX cwf_workflowitemrole_item_fk_idx ON cwf_workflowitemrole(workflowitem_id);


CREATE TABLE cwf_pooltask (
  pooltask_id   INTEGER DEFAULT nextval('cwf_pooltask_seq') PRIMARY KEY,
  workflowitem_id   INTEGER REFERENCES cwf_workflowitem(workflowitem_id),
  workflow_id   TEXT,
  step_id       TEXT,
  action_id     TEXT,
  eperson_id    INTEGER REFERENCES EPerson(eperson_id),
  group_id      INTEGER REFERENCES epersongroup(eperson_group_id)
);

CREATE INDEX cwf_pooltask_eperson_fk_idx ON cwf_pooltask(eperson_id);
CREATE INDEX cwf_pooltask_workflow_fk_idx ON cwf_pooltask(workflowitem_id);
CREATE INDEX cwf_pooltask_workflow_eperson_fk_idx ON cwf_pooltask(eperson_id,workflowitem_id);



CREATE TABLE cwf_claimtask (
  claimtask_id integer DEFAULT nextval('cwf_claimtask_seq') PRIMARY KEY,
  workflowitem_id integer REFERENCES cwf_workflowitem(workflowitem_id),
  workflow_id Text,
  step_id Text,
  action_id Text,
  owner_id integer REFERENCES eperson(eperson_id)
);

ALTER TABLE ONLY cwf_claimtask
ADD CONSTRAINT cwf_claimtask_unique UNIQUE (step_id, workflowitem_id, workflow_id, owner_id, action_id);

CREATE INDEX cwf_claimtask_workflow_fk_idx ON cwf_claimtask(workflowitem_id);
CREATE INDEX cwf_claimtask_workflow_eperson_fk_idx ON cwf_claimtask(workflowitem_id,owner_id);
CREATE INDEX cwf_claimtask_eperson_fk_idx ON cwf_claimtask(owner_id);
CREATE INDEX cwf_claimtask_workflow_step_fk_idx ON cwf_claimtask(workflowitem_id,step_id);
CREATE INDEX cwf_claimtask_workflow_step_action_fk_idx ON cwf_claimtask(workflowitem_id,step_id,action_id);
CREATE INDEX cwf_claimtask_workflow_step_action_eperson_fk_idx ON cwf_claimtask(workflowitem_id,step_id,action_id,owner_id);


CREATE TABLE cwf_in_progress_user (
  in_progress_user_id integer DEFAULT nextval('cwf_in_progress_user_seq') PRIMARY KEY,
  workflowitem_id integer REFERENCES cwf_workflowitem(workflowitem_id),
  user_id integer REFERENCES eperson(eperson_id),
  finished BOOL
);

ALTER TABLE ONLY cwf_in_progress_user
ADD CONSTRAINT cwf_in_progress_user_unique UNIQUE (workflowitem_id, user_id);

CREATE INDEX cwf_in_progress_user_workflow_fk_idx ON cwf_in_progress_user(workflowitem_id);
CREATE INDEX cwf_in_progress_user_eperson_fk_idx ON cwf_in_progress_user(user_id);
CREATE INDEX cwf_in_progress_user_workflow_eperson_fk_idx ON cwf_in_progress_user(workflowitem_id,user_id);
