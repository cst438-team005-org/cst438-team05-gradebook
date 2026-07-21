create table term (
      term_id  int primary key,
      tyear     int not null check (tyear between 2000 and 2030),
      semester varchar(10) not null check (semester in ('Spring', 'Fall')),
      add_date Date not null,
      add_deadline Date not null,
      drop_deadline Date not null,
      start_date Date not null,
      end_date Date not null
);

create table course (
    course_id varchar(10) primary key,
    title varchar(100) not null,
    credits int not null check (credits >= 0)
);

create table section (
     section_no int primary key,
     course_id varchar(10) not null,
     section_id int not null not null,
     term_id int not null not null,
     building varchar(10),
     room varchar(10),
     times varchar(25),
     instructor_email varchar(50),
     foreign key(course_id) references course(course_id),
     foreign key(term_id) references term(term_id)
);

create table user_table (
    id integer   primary key,
    name varchar(50) not null,
    email varchar(50) not null unique,
    password varchar(100) not null,
    type varchar(10) not null  check (type in ('STUDENT', 'ADMIN', 'INSTRUCTOR'))
);

create table enrollment (
    enrollment_id integer  primary key,
    grade varchar(5),
    section_no int not null,
    user_id int not null,
    foreign key(section_no) references section(section_no),
    foreign key(user_id) references user_table(id) on delete cascade
);

create sequence assignment_seq START WITH 6000;

create table assignment (
    assignment_id int  default next value for assignment_seq primary key,
    section_no int not null,
    title varchar(250) not null,
    due_date Date,
    foreign key (section_no) references section(section_no)
);

create sequence grade_seq START WITH 12000;

create table grade (
   grade_id int default next value for grade_seq primary key,
   enrollment_id int not null,
   assignment_id int not null,
   score int check (score between 0 and 100),
   foreign key(enrollment_id) references enrollment(enrollment_id) on delete cascade,
   foreign key(assignment_id) references assignment(assignment_id) on delete cascade
);
