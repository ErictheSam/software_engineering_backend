### ver 2.0接口调用规范

1. 连接服务器需要传送的是json格式数据，返回的也是json格式

2. 对于任何访问，需要如下的“公有参数”：

   * "type"，String型 指此次访问请求的类型
   * "userId"，int型 指此次访问方的用户名
   * "token"，String型 指为确保连接安全的独一无二的令牌，在登录成功后分配到用户

   如果这些不存在或者错误的话，则会报错。

3. 填充的请求类型（type参数的值）和其余参数如下：

   * "login"--登录

     * "token"固定为"1001"的String型变量
     * "username"，姓名
     * "password"，密码

     如果登录成功，则会以json的形式返回用户信息，但是返回类型都为String。参数如下：

     * "user_id"，用户id
     * "user_Username"，用户姓名
     * "user_Password"，用户密码
     * "user_Gender"，用户性别，1为男性2为女性3为其它
     * "user_Department"，用户院系
     * "user_Birthday"，用户生日
     * "user_HaveVoice"，用户是否已有录音，1为没有2为有
     * "user_Rank"，用户类别，（学生:1 教师:2 管理员:3）
     * "newToken"，服务器返回的随机生成独一无二的token，该token需在之后用户的操作中用到。

   * "registerUser"--管理员注册用户

     注册用户和注册课程这两个“批量添加”的接口都是通过jsonarray来进行交互的，该jsonarray的参数需要为"content"。jsonarray内存放的各个jsonobject成员，即每个用户需要添加的的参数如下：

     * "rank"，int型 用户类别
     * "id"，int型 用户id
     * "username"，String型 用户姓名
     * "password"，String型 用户密码
     * "gender"，int型 性别
     * "department"，String型 院系
     * "birthday"，String型 生日
     * "newToken"，登录获得的token

     同时，为了确定权限，还需要一个放在主object里的int型参数"maxrank"，用来确定一批量需要注册用户的最大权限

   * "registerCourse"--管理员注册课程，其"content"的每个成员参数为

     * "id"，int型 课程id
     * "TeacherId"，int型 授课教师id
     * "room"， String型 课程教室
     * "coursename"， String型 课程名称
     * "students"，jsonarray 课程的学生们
       * "students"的各个jsonobject成员都只有一个int型参数："studentId"，即学生的id。

   * "modifyUserInfo"--管理员改变用户信息

     需要的参数为：

     * "id"，int型 用户的id
     * "password"，string型，更改后的密码
     * "department"，string型，更改后的院系
     * "birthday"，string型，更改后的生日

   * "modifyCourseInfo"--管理员改变课程信息

     需要的参数为：

     * "id"，int型 课程的id
     * "teacherid"，int型 新教师的id
     * "capacity"，int型 课程新的容量
     * "classroom"，string型 课程新的教室

   * "setCourseTime"--管理员增加/删除一项课程的时间

     需要的参数如下：

     * "id" int型 课程id
     * "modifyType" int型 操作类型，0为删除操作，1为添加操作
     * "classtime" 为JSONArray型变量，该JSONArray变量的每一个成员为含有如下参数的JSONObject:
     * "start_time"，String型 "YY-mm-dd hh:MM:ss"格式的课程开始时间
     * "end_time"，String型 同样格式的课程结束时间

   * "registerStudent2Course"--管理员将学生添加到课程中，需要的参数如下：

     * "studentId" int型 学生编号
     * "courseId" int型 课程学生编号

   * "deleteStudent2Course"--管理员将学生移除出课程，参数同上

   * "assign"--学生签到

     * "registerTime" String型 格式同上的签到时间，后台会自动判断签到哪节课

     返回带有如下额外信息，变量也均为String型：

     * "course_time_start"，签到课程的开始时间
     * "course_time_end"，签到课程的截止时间
     * "course_id"，签到课程的id名称
     * "course_name"，签到课程的课程名
     * "student_assign"，学生签到情况，一般为"1"(已签到)

   * "deleteCourse"--管理员删除课程

     * "courseId"，int型，课程id

   * "deleteUser"--管理员删除用户

     * "deleteId"，int型，用户id

   * "getAllCourses"--得到全部课程，不需要额外参数，得到的"contents"参数为JSONArray，该array的成员参数如下String：

     * "course_id"，课程名字
     * "course_TeacherId"，老师id
     * "course_Teachername"，老师名字
     * "course_Capacity"，课程容量
     * "course_Room"，课程教室
     * "course_name"，课程名字

   * "getAllUsers"--管理员得到全部非管理员用户，不需要额外参数，得到的"contents"参数为JSONArray，该array的成员参数如下，皆为String：

     * "user_id"，用户id
     * "user_Username"，用户姓名
     * "user_Password"，用户密码
     * "user_Gender"，用户性别，1为男性2为女性3为其它
     * "user_Department"，用户院系
     * "user_Birthday"，用户生日
     * "user_HaveVoice"，用户是否已有录音，1为没有2为有
     * "user_Rank"，用户类别，（学生:1 教师:2 管理员:3）

   * "getRecordByStudent"--管理员或学生本人得到一个学生的所有签到记录

     需要"studentId" int型参数，表示学生的id

     返回的信息的"contents"参数为jsonarray型，存有每一条记录，均为string型

     - "course_time_start"，签到课程的开始时间
     - "course_time_end"，签到课程的截止时间
     - "course_id"，签到课程的id名称
     - "course_name"，课程的名称
     - "student_assign"，学生签到情况，1为签了0为没签

   * "getRecordByCourse"--管理员或该课程老师得到课程的所有签到记录

     需要"courseId" int型参数，表示课程的id

     返回的信息的"contents"参数为jsonarray型，存有每一条记录，均为string型

     - "course_time_start"，签到课程的开始时间
     - "course_time_end"，签到课程的截止时间
     - "student_id"，学生的id名称
     - "student_name"，学生的姓名
     - "student_assign"，学生签到情况，1为签了0为没签

   * "getAllStudentByCourse"--管理员或课程教师得到一门课程全部的学生

     需要"courseId" int型参数，表示课程的id

     返回信息的"contents"参数内记录如下，均为String类型

     * "student_id"，学生id
     * "student_name"，学生姓名
     * "student_assign"，学生总共签到次数

   * "getNotAssignedStudent"--管理员或者任课教师查看一节课没有到的学生

     需要的参数如下：

     * "courseId"，课程id
     * "start_time"，String型，课程开始时间

     返回的"contents"参数如下，皆为String

     - "course_time_start"，签到课程的开始时间
     - "course_time_end"，签到课程的截止时间
     - "student_id"，学生的id名称
     - "student_name"，学生的姓名
     - "student_assign"，学生签到情况，1为签了0为没签

   * "getOngoingClass"--教师查看正在上着的课

     需要的参数为"timenow" String型，当下时间

     返回的参数如下，均为String型：

     * "course_time_start"，课程开始时间
     * "course_time_end"，课程结束时间
     * "course_id"，课程编号
     * "course_name"，课程名字

   * "getCourseInfo"--任何人都可以查看课程信息

     需要参数为"id", int型，课程id

     返回的参数如下，均为String型：

     * "course_id"，课程名字
     * "course_TeacherId"，老师id
     * "course_Teachername"，老师名字
     * "course_Capacity"，参与课程学生数
     * "course_Room"，课程教室
     * "course_name"，课程名字
     * "classtime"，JSONArray型，课程时间
       - 该JSONArray的每一个成员拥有参数如下：
         - "course_time_start"
         - "course_time_end"

   * "teacherAddAssign"--教师对学生补签到

     需要参数"studentId" int型 学生id；"courseId" int型 教师id；

     "registerTime" String型，"YY-mm-dd hh:MM:ss"格式的课程开始时间， 

     返回参数如下，均为String型：

     - "course_time_start"，签到课程的开始时间
     - "course_time_end"，签到课程的截止时间
     - "course_id"，签到课程的id名称
     - "course_name"，签到课程的课程名
     - "student_assign"，学生签到情况，为"1"(已签到)

   * "getCourseTime"--任何人查看课程所有时间

     需要的参数为"courseId"，int型，课程id

     返回"contents"参数为一个JSONArray，其成员的参数皆为String型

     - "course_time_start"，每一节课的开始时间
     - "course_time_end"，每一节课的结束时间

   * "getEveryCourseInfo"--管理员或学生教师本人得到全部的课程

     需要"getId" int型参数，表示用户的id

     返回信息的"contents"参数内记录如下，除最后一条之外均为String类型：

     - "course_id"，课程名字
     - "course_TeacherId"，老师id
     - "course_Teachername"，老师名字
     - "course_Capacity"，参与课程人数
     - "course_Room"，课程教室
     - "course_name"，课程名字
     - "classtime"，JSONArray型，课程所有时间
       - 该JSONArray的每一个成员拥有参数如下：
         - "course_time_start"
         - "course_time_end"

   * "getAssignClassRecord"--管理员或者任课教师或者学生本人查看一个学生所有课次的签到情况。需要的参数如下：

     - "courseId"，int型，课程id
     - "studentId"，int型，学生id

     返回的"contents"参数的成员如下，皆为String

     - "course_time_start"，签到课程的开始时间
     - "course_time_end"，签到课程的截止时间
     - "course_id"，签到课程的id名称
     - "course_name"，课程的名称
     - "student_assign"，学生签到情况，1为签了0为没签

   * "logout"--登出

   对于所有请求，返回的json里都会采用"message"参数值来传递操作的结果。