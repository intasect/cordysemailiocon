select * from context_container
select * from email_box
select * from email
select * from trigger_store

delete from trigger_store
delete from email
delete from context_container
delete from email_box

select   *
from     CONTEXT_CONTAINER
where    PROCESSING_STATUS = 'COMPLETED'
and      STATUS_CHANGE_DATE < :ToSTATUS_CHANGE_DATE
order by STATUS_CHANGE_DATE desc


update CONTEXT_CONTAINER set processing_status = 'COMPLETED' where processing_status = 'COMPLETE'