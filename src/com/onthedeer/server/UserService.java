package com.onthedeer.server; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import java.util.List;
import java.util.LinkedList;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLEncoder;
import java.net.URLDecoder;

import java.util.Date;
import java.util.TimeZone;

public class UserService {
	 
	private static final Logger LOGGER = LogManager.getLogger(UserService.class);
	private static final String message = "message";
	private static final String encodingMethod = "UTF-8";
	private static final String encode_error = "encode error";

	private void closeResult(ResultSet rs){
		try{
			if(rs != null)
				rs.close();
		}catch(Exception e){
			LOGGER.error("Failed to close the resultSet:",e);
		}
	}

	protected String getCurrentTime(){
		SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		a.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		Date date = new Date(System.currentTimeMillis());
		String current_time = a.format(date);
		return current_time;		
	}

	public int find(Object userId, String sql, boolean isInt, Connection conn){

		final int findSuccess = 1;
		final int notFound = 2;
		final int sqlFailed = 3;
		ResultSet rs = null;
		
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			if(isInt)			
				ps.setInt(1, (Integer)userId);
			else
				ps.setString(1,(String)userId);
			rs = ps.executeQuery();
			if(rs.next()){
				return findSuccess;
			}else{
				return notFound;
			}
		}catch (SQLException e) {
			LOGGER.error("Failed to query:",e);
			return sqlFailed;
		}finally{
			closeResult(rs);
		}
	}

	private int manipulateMeta( Object id, String sql, boolean isInt, Connection conn){
		
		final int manipulateSuccess = 1;
		final int manipulateFailed = 2;
		final int sqlFailed= 3;

		try(PreparedStatement ps = conn.prepareStatement(sql)){
			if(isInt)
				ps.setInt(1,(Integer)id);
			else
				ps.setString(1,String.valueOf(id));
			int out = ps.executeUpdate();
			if(out == 0)
				return manipulateFailed;
			else
				return manipulateSuccess;
		}catch (SQLException e){
			LOGGER.error("Failed to operate command '"+sql+"'",e);
			return sqlFailed;
		}
	}

	private int manipulateStatement( String sql, Connection conn ){

		final int sqlFailed = -1;
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			return ps.executeUpdate();
		}catch (SQLException e){
			LOGGER.error("Failed to operate statement '"+sql+"'",e);
			return sqlFailed;
		}
	}

	private List<Object> findAllId(String sql,String param,boolean isInteger,Connection conn){

		List<Object> list = new LinkedList<>();

		ResultSet set = null;

		try(PreparedStatement ps = conn.prepareStatement(sql)){
			set = ps.executeQuery();
			while(set.next()){
				if(isInteger)
					list.add(set.getInt(param));
				else
					list.add(set.getString(param));
			}
		}catch(SQLException e){
			LOGGER.error("Find infomation error:",e);
		}finally{
			closeResult(set);
		}
		return list;
		
	}

	private void getMetaInfo(ResultSet rs, JSONObject jsonObject) throws SQLException{
		ResultSetMetaData metaData= rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		for( int i = 1; i <= columnCount; i ++){
			String columnName =metaData.getColumnLabel(i);
            String value = rs.getString(columnName);
			jsonObject.put(columnName,value);
			}
		jsonObject.put(message,"成功");
	}

	public int findInt(String param, String sql, Connection conn){
		
		ResultSet rs = null;

		try(PreparedStatement ps = conn.prepareStatement(sql)){
			rs = ps.executeQuery();
			if(rs.next()){
				return rs.getInt(param);
			}else{
				return 0;
			}
		}catch(SQLException e){
			LOGGER.error("Failed to find int",e);
			return 0;
		}finally{
			closeResult(rs);
		}
	}

	protected int findRank(int userId, Connection conn){// 身份就是这样！
		return findInt("user_Rank","select * from user_info where user_id="+userId,conn);
	}

	private void manipulateRegister( boolean isUser, int signal, int rank, JSONObject json ){
        String paramType = "";
        if(!isUser){
            paramType = "门课";
        }else{
            paramType = "个用户";
        }
        switch(signal){
            case(2):
                json.put(message,"第"+(rank+1)+"门课教师不存在");
                break;
            case(3):
                json.put(message,"注册第"+(rank+1)+paramType+"时数据库错误");
                break;
            case(4):
                json.put(message,"第"+(rank+1)+paramType+"已存在");
                break;
            default:
                json.put(message,"第"+(rank+1)+paramType+"JSON参数错误");
        }
    }

	private String getParam( int id, String sql, String param, Connection conn ){// Param是得到东西
		
		ResultSet rs = null;
		try( PreparedStatement ps = conn.prepareStatement( sql ) ){
			ps.setInt(1,id);
			rs = ps.executeQuery();
			if(rs.next()){
				return rs.getString(param);
			}else{
				return null;
			}
		}catch(SQLException e){
			LOGGER.error("Failed to find param",e);
			return null;
		}finally{
			closeResult(rs);
		}
	}

	public String login(int id, String passWord){

		JSONObject jsonObject = new JSONObject();
		
		Connection conn = DBUtil.getConn();
		String sql="select * from user_info where user_id = ? and user_Password=?";
		
		ResultSet rs = null;

		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setInt(1,id);
			ps.setString(2, passWord);
			rs = ps.executeQuery();
			if(rs.next()){
				getMetaInfo(rs,jsonObject);
			}else{
				jsonObject.put(message,"登录失败...\n请检查学号，用户名和密码是否正确");
			}
		} catch (SQLException e) {
			LOGGER.error("Failed to query:",e);
			jsonObject.put(message,"登录失败...\n数据库错误");
		}finally{
			DBUtil.closeConn();
			closeResult(rs);
		}
		return jsonObject.toString();
	}

	public int deleteCourseFromTeacher( int courseId, int teacherId, Connection conn){
		
		int deleteTeacherCourse = manipulateMeta(Integer.valueOf(courseId),"delete from "+teacherId+"_course where course_id=?",true,conn);
		int deleteTeacherRecord = manipulateMeta(Integer.valueOf(courseId),"delete from "+teacherId+"_courseTime where course_id=?",true,conn);

		if(deleteTeacherCourse == 3 || deleteTeacherRecord == 3){
			return -1;
		}
		return 0;
	}

	public int insertCourseToTeacher( int courseId, String courseName, int teacherId, Connection conn){

		int insertTeacherCourse = manipulateMeta(
			courseName,"insert into "+teacherId+"_course ( course_id, course_name ) values ("+courseId+",?)",false, conn
		);
		int insertTeacherTime = manipulateStatement(
			"insert into "+teacherId+"_courseTime select course_time_start, course_time_end, course_id, course_name from "+courseId+"_time",conn
		);
		if( insertTeacherCourse == 3 || insertTeacherTime == -1){
			return -1;
		}
		return 0;
	}


	private int modifyCourse(JSONObject jsonObject, String sql, boolean isInsert,  Connection conn){
		final int modifySuccess = 1;
		final int modifyFailed = 2;
		final int sqlFailed = 3;
		final int jsonParameterError = 5;
		final int notLegalTeacher = 4;

		int id = jsonObject.getIntValue("id");
		int teacherid = jsonObject.getIntValue("TeacherId");

		String teacherName ="";
		String classroom = jsonObject.getString("room");
		
		String courseName ="";

		if(isInsert){
			teacherName = jsonObject.getString("teacher_name");
			courseName = jsonObject.getString("coursename");
			if(teacherName == null || courseName == null)
				return jsonParameterError;
			try{
				courseName = URLEncoder.encode(courseName,encodingMethod);
			}catch(Exception e){
				LOGGER.error(encode_error,e);
			}
		}
		if( id *teacherid == 0 || classroom == null || courseName == null){
			return jsonParameterError;
		}
		try{
			classroom= URLEncoder.encode(classroom,encodingMethod);
		}
		catch(Exception e){
			LOGGER.error(encode_error,e);
		}
		if(!isInsert){
			teacherName = getParam(teacherid,"select * from user_info where user_id = ?","user_Username",conn);
			if( teacherName == null || findRank(teacherid,conn) != 2){
				return notLegalTeacher;
			}
			int oldTeacherId = findInt("course_TeacherId","select * from course_info where course_id ="+id,conn);
			if( oldTeacherId != teacherid ){
				int del = deleteCourseFromTeacher(id,oldTeacherId,conn);
				int join = insertCourseToTeacher(id,teacherName,teacherid,conn);
				if(del == -1 || join == -1){
					return sqlFailed;
				}
			}
		}
	
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			if(isInsert){
				ps.setInt(1,id);
				ps.setInt(2,teacherid);
				ps.setString(3,classroom);
				ps.setString(4,courseName);
				ps.setString(5,teacherName);
			}else{
				ps.setInt(1,teacherid);
				ps.setString(2,classroom);
				ps.setString(3,teacherName);
				ps.setInt(4,id);
			}
			int result = ps.executeUpdate();
			if(result != 0)
				return modifySuccess;
			else
				return modifyFailed;
		}catch(SQLException e){
			LOGGER.error("Caused an exception when modifying course:",e);
			return sqlFailed;
		}
	}

	private int registerCourse(JSONObject jsonObject,Connection conn){// add up a class

		final int registerFailed = 2;
		final int sqlFailed = 3;
		final int alreadyExist = 4;
		final int jsonParameterError = 5;
		
		int id = jsonObject.getIntValue("id");
		String courseName = jsonObject.getString("coursename");
		int teacherid = jsonObject.getIntValue("TeacherId");

		JSONArray arr = jsonObject.getJSONArray("students");

		if( id *teacherid == 0 || courseName == null || arr == null ){
			return jsonParameterError;
		}
		try{
			courseName = URLEncoder.encode(courseName,encodingMethod);
		}catch(Exception e){
			LOGGER.error(encode_error,e);
		}

		int findCourse = find(Integer.valueOf(id), "select * from course_info where course_id = ?",true,conn);

		String teacherName = getParam(teacherid,"select * from user_info where user_id = ?","user_Username",conn);

		if( findCourse == 1 ){
			return alreadyExist;
		}else if( findCourse == 3 ){
			return sqlFailed;
		}else if( teacherName == null ){
			return registerFailed;
		}

		int teacherRank = findRank(teacherid, conn);// 是否注册的是有资质的教师
		if(teacherRank != 2){
			return registerFailed;
		}

		jsonObject.put("teacher_name",teacherName);

		int registerCourseTime = manipulateStatement(
			"create table if not exists "+ id +"_time ( course_time_start varchar(20) not null, course_time_end varchar(20) not null, course_id int not null,course_name varchar(64) not null, student_assign tinyint not null )engine=InnoDB default charset=utf8",conn
			);
		int registerStuList = manipulateStatement(
			"create table if not exists "+id+"_student ( student_id int not null, student_name varchar(64) not null, student_assign int not null ) engine=InnoDB default charset=utf8",conn
			);
		int registerCourseRecord = manipulateStatement(
			"create table if not exists "+id+"_course_record ( course_time_start varchar(20) not null, course_time_end varchar(20) not null, student_id int not null, student_name varchar(64) not null, student_assign tinyint not null) engine=InnoDB default charset=utf8", conn
		);

		if(registerCourseTime == -1 || registerStuList == -1 || registerCourseRecord == -1){
			return sqlFailed;
		}
		int insertTeacher = insertCourseToTeacher(id, courseName, teacherid, conn);
		if(insertTeacher == -1){
			return sqlFailed;
		}
		String sql = "insert into course_info (course_id,course_TeacherId,course_Capacity,course_Room,course_name,course_Teachername) values(?,?,0,?,?,?)";
		int result = modifyCourse(jsonObject,sql,true,conn);
		return result;

	}

	public String registerBatchCourse(JSONObject json){
		JSONArray arr = json.getJSONArray("content");
		JSONObject back = new JSONObject();
		if(arr == null){
			back.put(message,"JSON参数错误");
			return back.toString();
		}
		int courseNum = arr.size();
		Connection conn = DBUtil.getConn();
		for( int i = 0; i < courseNum; i ++){
			JSONObject courseInfo = arr.getJSONObject(i);
			int courseId = courseInfo.getIntValue("id");
			int result = registerCourse(courseInfo,conn);
			if(result != 1){
				manipulateRegister(false,result,i,back);
				DBUtil.closeConn();
				return back.toString();
			}
			JSONArray arr2 = courseInfo.getJSONArray("students");
			int stuSize = arr2.size();
			for( int j = 0; j < stuSize; j ++ ){
				JSONObject js = arr2.getJSONObject(j);
				int stu = js.getIntValue("studentId");
				if(stu != 0){
					int result2 = registerStu2Course(stu,courseId);
					if(result2 != 1){
						if(result2 == 2){
							back.put(message,"第"+(i+1)+"门课的第"+(j+1)+"个学生添加失败");
						}else if(result2 == 3){
							back.put(message,"第"+(i+1)+"门课的第"+(j+1)+"个学生添加时数据库出错");
						}else if(result2 == 4){
							back.put(message,"第"+(i+1)+"门课的第"+(j+1)+"个学生添加重复");							
						}else if(result2 == 5){
							back.put(message,"第"+(i+1)+"门课的第"+(j+1)+"个学生不存在");
						}else{
							back.put(message,"第"+(i+1)+"门课的第"+(j+1)+"个学生身份不合法");
						}
						DBUtil.closeConn();
						return back.toString();
					}
				}
			}
		}
		DBUtil.closeConn();
		back.put(message,"注册成功");
		return back.toString();
	
	}

	public int modifyCourseInfo( JSONObject jsonObject ){
		String sql = "update course_info set course_TeacherId = ?, course_Room = ?, course_Teachername = ? where course_id = ?";
		Connection conn = DBUtil.getConn();
		int result = modifyCourse(jsonObject,sql, false, conn);
		DBUtil.closeConn();
		return result;
	}

	private int modifyUser(JSONObject json, String sql, boolean isInsert, Connection conn){// 默认rank不能改
		final int modifySuccess = 1;
		final int modifyFailed = 2;
		final int sqlFailed = 3;
		final int jsonParameterError = 5;

		int id = json.getIntValue("id");
		String userName="";
		String passWord = json.getString("password");
		int gender = 0;
		String department = json.getString("department");
		String birthday = json.getString("birthday");
		int rank = 0;

		if(isInsert){
			rank = json.getIntValue("rank");
			userName = json.getString("username");
			gender = json.getIntValue("gender");//1=Male,2=Female,3=Others
			if(rank*gender == 0 || userName == null)
				return jsonParameterError;
			try{
				userName = URLEncoder.encode(userName,encodingMethod);
			}catch(Exception e){
				LOGGER.error(encode_error,e);
			}
		}

		if( id == 0 || passWord == null || department == null || birthday == null){
			return jsonParameterError;
		}
		try{
			department = URLEncoder.encode(department,encodingMethod);
		}
		catch(Exception e){
			LOGGER.error(encode_error,e);
		}

		try(PreparedStatement ps = conn.prepareStatement(sql)){
			if(isInsert){
				ps.setInt(1,id);
				ps.setString(2, userName);
				ps.setString(3, passWord);
				ps.setInt(4,gender);
				ps.setString(5,department);
				ps.setString(6,birthday);
				ps.setInt(7,rank);
			}else{
				ps.setString(1,passWord);
				ps.setString(2,department);
				ps.setString(3,birthday);
				ps.setInt(4,id);
			}
			int result = ps.executeUpdate();
			if(result != 0)
				return modifySuccess;
			else
				return modifyFailed;
		}catch (SQLException e) {
			LOGGER.error("Caused an error when modifying user:",e);
			return sqlFailed;
		}
	}

	private int registerUser(JSONObject json, Connection conn){
		
		final int sqlFailed = 3;
		final int alreadyExist = 4;
		final int jsonParameterError = 5;
		final int notAllowed = 6;

		int id = json.getIntValue("id");

		int rank = json.getIntValue("rank");
	
		if( id * rank== 0 ){
			return jsonParameterError;
		}
		
		int findResult = find( Integer.valueOf(id),"select * from user_info where user_id=?",true,conn);

		if(findResult == 1){
			return alreadyExist;
		}else if(findResult == 3){
			return sqlFailed;
		}

		if( rank > 0 && rank < 3){
			int createCourseTable = manipulateStatement(
					"create table if not exists "+id+"_course (course_id int not null,course_name varchar(64) not null) engine=InnoDB default charset=utf8",conn
			);

			if(createCourseTable == -1){
				return sqlFailed;
			}
			if(rank == 1){
				int createPersonalTable = manipulateStatement(
					"create table if not exists "+ id +"_record ( course_time_start varchar(20) not null, course_time_end varchar(20) not null, course_id int not null, course_name varchar(64) not null, student_assign tinyint not null )engine=InnoDB default charset=utf8",conn
				);

				if(createPersonalTable == -1){
					return sqlFailed;
				}
			}
			else{
				int createTeacherTable = manipulateStatement(
					"create table if not exists "+id+"_courseTime ( course_time_start varchar(20) not null, course_time_end varchar(20) not null, course_id int not null, course_name varchar(64) not null) engine=InnoDB default charset=utf8",conn
				);
				if(createTeacherTable == -1){
					return sqlFailed;
				}
			}
		}else if(rank != 3){
			return notAllowed;
		}

		String sql = "insert into user_info (user_id, user_Username,user_Password,user_Gender, user_Department,user_Birthday,user_HaveVoice,user_Rank) values (?,?,?,?,?,?,1,? )";

		return modifyUser(json,sql,true,conn);
	}

	public String registerBatchUser(JSONObject json){
		JSONObject back = new JSONObject();
		JSONArray arr = json.getJSONArray("content");
		int maxRank = json.getIntValue("maxrank");
		if( arr == null ){
			back.put(message,"JSON参数错误");
			return back.toString();
		}
		int userNum = arr.size();
		Connection conn = DBUtil.getConn();
		for( int i = 0; i < userNum; i ++ ){
			JSONObject userInfo = arr.getJSONObject(i);
			int rank = userInfo.getIntValue("rank");
			if(rank > maxRank){
				back.put(message,"注册失败...\n您无权限注册第"+(i+1)+"个用户");
				return back.toString();
			}
			int result = registerUser(userInfo,conn);
			if(result != 1){
				manipulateRegister(true,result,i,back);
				DBUtil.closeConn();
				return back.toString();
			}
		}
		DBUtil.closeConn();
		back.put(message,"注册成功");
		return back.toString();
	}

	public int modifyUserInfo(JSONObject json){
		String sql = "update user_info set user_Password = ?,user_Department=?,user_Birthday=?,user_HaveVoice=0 where user_id=?";
		Connection conn = DBUtil.getConn();
		int modifyResult = modifyUser(json,sql,false,conn);
		DBUtil.closeConn();
		return modifyResult;
	}

	public int modifyTime(JSONObject json){
		
		final int setSuccess = 1;
		final int setFailed = 2;
		final int sqlFailed = 3;
		final int jsonFailed = 5;
		final int dateFormatFailed = 4;

		int courseId = json.getIntValue("id");

		Integer mod = json.getInteger("modifyType");

		String name = "";

		JSONArray classtime = json.getJSONArray("classtime");

		int timeSize = classtime.size();

		if(courseId * timeSize == 0 || classtime == null || mod == null)
			return jsonFailed;

		Connection conn = DBUtil.getConn();

		int teacherId = findInt("course_TeacherId","select * from course_info where course_id="+courseId,conn);
		
		if(mod != 0){
			name = getParam(courseId,"select * from course_info where course_id=?","course_name",conn);//课程名字返回来
			if(name == null)
				return setFailed;
		}

		int firstFind = find(Integer.valueOf(courseId), "select * from course_info where course_id = ?", true, conn );//是不是存在呢？
		if(firstFind != 1){
			DBUtil.closeConn();
			return firstFind;
		}

		List<Object> studentId = findAllId("select * from "+courseId+"_student","student_id",true,conn);

		boolean dateFormatError = false;
		boolean readyMod;

		for( int i = 0; i < timeSize; i ++){
			
			JSONObject pertime = classtime.getJSONObject(i);
			String start = pertime.getString("start_time");
			String end = pertime.getString("end_time");
			
			if(start != null && end != null){
				readyMod = true;
				try{
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					format.setLenient(false);
					format.parse(start);
					format.parse(end);
				} catch(ParseException e){
					DBUtil.closeConn();
					LOGGER.error("Time format error",e);
					dateFormatError = true;
					readyMod = false;
				}
				if(readyMod){
					String sql; 
					if(mod == 0){
						sql = "delete from "+courseId +"_time where course_time_start = ?";
					}
					else
						sql = "insert into "+courseId+"_time( course_time_start,course_time_end,course_id,course_name,student_assign) select ?,?,?,?,0 from dual where not exists(select * from "+courseId+"_time where course_time_start=?)";
					try(PreparedStatement ps = conn.prepareStatement(sql)){
						ps.setString(1,start);
						if(mod != 0){
							ps.setString(2,end);
							ps.setInt(3,courseId);
							ps.setString(4,name);
							ps.setString(5,start);
						}
						ps.executeUpdate();
					}catch(SQLException e){
						LOGGER.error("Failed to modify into chart time",e);
						DBUtil.closeConn();
						return sqlFailed;
					}
					String sql4;
					if(mod == 0)
						sql4 = "delete from "+teacherId+"_courseTime where course_time_start=?";
					else
						sql4 = "insert into "+teacherId+"_courseTime (course_time_start,course_time_end,course_id,course_name) select ?,?,?,? from dual where not exists(select * from "+teacherId+"_courseTime where course_time_start=?)";
					try(PreparedStatement ps4 = conn.prepareStatement(sql4)){
						ps4.setString(1,start);
						if(mod != 0){
							ps4.setString(2,end);
							ps4.setInt(3,courseId);
							ps4.setString(4,name);
							ps4.setString(5,start);
						}
						ps4.executeUpdate();
					}catch(SQLException e){
						LOGGER.error("Failed to modify into teacher record",e);
						DBUtil.closeConn();
						return sqlFailed;
					}
					if( studentId.size()>0 ){
						for( Object a:studentId){
							Integer id = (Integer)a;
							String stuName = getParam(id,"select * from user_info where user_id=?","user_Username",conn);
							String sql2;
							if(mod == 0)
								sql2 = "delete from "+id+"_record where course_time_start=? and course_id=?";
							else
								sql2 = "insert into "+id+"_record( course_time_start,course_time_end,course_id,course_name,student_assign) select ?,?,?,?,0 from dual where not exists(select * from "+id+"_record where course_time_start=? and course_id=?)";
							try(PreparedStatement ps2 = conn.prepareStatement(sql2)){
								ps2.setString(1,start);
								if(mod == 0){
									ps2.setInt(2,courseId);
								}else{
									ps2.setString(2,end);
									ps2.setInt(3,courseId);
									ps2.setString(4,name);
									ps2.setString(5,start);
									ps2.setInt(6,courseId);
								}
								ps2.executeUpdate();
							}catch(SQLException e){
								LOGGER.error("Failed to modify into student chart",e);
								DBUtil.closeConn();
								return sqlFailed;
							}
							String sql3;
							if(mod == 0)
								sql3 = "delete from "+courseId+"_course_record where course_time_start=? and student_id="+id;//没有学生了
							else
								sql3 = "insert into "+courseId+"_course_record (course_time_start,course_time_end,student_id,student_name,student_assign) select ?,?,"+id+",?,0 from dual where not exists(select * from "+courseId+"_course_record where course_time_start=? and student_id ="+id+")";
							try(PreparedStatement ps3 = conn.prepareStatement(sql3)){
								ps3.setString(1,start);
								if(mod != 0){
									ps3.setString(2,end);
									ps3.setString(3,stuName);
									ps3.setString(4,start);
								}
								ps3.executeUpdate();
							}catch(SQLException e){
								LOGGER.error("Failed to modify into course record",e);
								DBUtil.closeConn();
								return sqlFailed;
							}
						}
					}
				}
			}else{
				DBUtil.closeConn();
				return jsonFailed;
			}
		}

		DBUtil.closeConn();
		if(dateFormatError)
			return dateFormatFailed;
		return setSuccess;
	}

	private int threeEnsure( int studentId, int courseId, Connection conn){

		final int noRegister = 1;
		final int alreadyRegistered = 4;
		final int sqlFailed = 3;
		final int notExist = 5;
		final int notStudent = 6;

		int firstFind = find(Integer.valueOf(courseId),"select * from course_info where course_id = ?",true,conn);

		int secondFind = find(Integer.valueOf(studentId),"select * from user_info where user_id = ?",true,conn);

		if(firstFind == 2 || secondFind == 2){
			return notExist;
		}else if(firstFind == 3 || secondFind == 3){
			return sqlFailed;
		}

		int studentRank = findRank(studentId,conn);
		if(studentRank != 1){
			return notStudent;
		}

		int thirdFind = find(Integer.valueOf(courseId), "select * from "+studentId+"_course where course_id = ?", true, conn);

		if(thirdFind == 1){
			return alreadyRegistered;
		}else if(thirdFind == 3){
			return sqlFailed;
		}else{
			return noRegister;
		}
	}

	public int registerStu2Course(int studentId, int courseId){
		
		final int registerSuccess = 1;
		final int registerFailed = 2;
		final int sqlFailed = 3;

		Connection conn = DBUtil.getConn();

		int checkResult = threeEnsure(studentId,courseId,conn);

		if(checkResult != 1){
			DBUtil.closeConn();
			return checkResult;
		}

		String courseName = getParam(courseId,"select * from course_info where course_id=?","course_name",conn);

		String studentName = getParam(studentId,"select * from user_info where user_id=?","user_Username",conn);

		int insertCourse = manipulateMeta(
			courseName,"insert into "+studentId+"_course (course_id,course_name) values ("+courseId+",?)",false,conn
			);

		int insertStudent = manipulateMeta(
			studentName,"insert into "+courseId+"_student (student_id,student_name,student_assign) values ("+studentId+",?,0)",false,conn
			);

		if(insertCourse == 2 || insertStudent == 2){
			DBUtil.closeConn();
			return registerFailed;
		}else if(insertCourse == 3 || insertStudent == 3){
			DBUtil.closeConn();
			return sqlFailed;
		}

		int insertAllCoursetime = manipulateStatement(
			"insert into "+studentId+"_record select * from "+courseId +"_time",conn       
		);

		if(insertAllCoursetime == -1){
			return sqlFailed;
		}

		List<Object> startTimeList = findAllId("select * from "+courseId+"_time", "course_time_start",false,conn);
		List<Object> endTimeList = findAllId("select * from "+courseId+"_time", "course_time_end",false,conn);

		String sql = "insert into "+courseId+"_course_record (course_time_start, course_time_end, student_id, student_name, student_assign) select ?,?,"+studentId+",?,0 from dual where not exists(select * from "+courseId+"_course_record where course_time_start=? and student_id ="+studentId+")";
		int t = startTimeList.size();
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			if(startTimeList != null && t > 0){
				for(int s = 0; s < t; s ++){
					String startTime = (String) startTimeList.get(s);
					String endTime = (String) endTimeList.get(s);
					ps.setString(1,startTime);
					ps.setString(2,endTime);
					ps.setString(3,studentName);
					ps.setString(4,startTime);
					ps.executeUpdate();
				}
			}
		}catch(SQLException e){
			DBUtil.closeConn();
			LOGGER.error("failed to update course_record",e);
			return sqlFailed;
		}
		int newCapacity = manipulateMeta(Integer.valueOf(courseId),"update course_info set course_Capacity = course_Capacity + 1 where course_id = ?",true,conn);
		DBUtil.closeConn();
		if( newCapacity == 3){
			return sqlFailed;
		}
		return registerSuccess;
	}

	public int deleteStu2Course(int studentId, int courseId){
		
		final int deleteSuccess = 1;
		final int deleteFailed = 2;
		final int sqlFailed = 3;
		final int alreadyDeleted = 4;

		Connection conn = DBUtil.getConn();

		int checkResult = threeEnsure(studentId,courseId,conn);

		if(checkResult != 4){
			DBUtil.closeConn();
			if(checkResult == 1){
				return alreadyDeleted;
			}
			return checkResult;
		}

		int deleteCourse = manipulateMeta(Integer.valueOf(courseId),"delete from "+studentId+"_course where course_id = ?",true, conn);

		int deleteStudent = manipulateMeta(Integer.valueOf(studentId),"delete from "+courseId+"_student where student_id=?",true,conn);		
		
		if( deleteCourse == 2 || deleteStudent == 2 ){
			DBUtil.closeConn();
			return deleteFailed;
		}else if(deleteCourse == 3 || deleteStudent == 3){
			DBUtil.closeConn();
			return sqlFailed;
		}

		int deleteRecord = manipulateMeta(Integer.valueOf(courseId),"delete from "+studentId+"_record where course_id=?",true,conn);
		
		if(deleteRecord == 3){
			DBUtil.closeConn();
			return sqlFailed;
		}
		
		int deleteCourseRecord = manipulateMeta(Integer.valueOf(studentId),"delete from "+courseId+"_course_record where student_id=?",true,conn);
		int newCapacity = manipulateMeta(Integer.valueOf(courseId),"update course_info set course_Capacity = course_Capacity - 1 where course_id = ?",true,conn);
		DBUtil.closeConn();
		if(deleteRecord == 3 || newCapacity == 3 ){
			return sqlFailed;
		}
		return deleteSuccess;
	}

	public int deleteStudent(int studentId) {
		
		final int deleteSuccess = 1;
		final int deleteFailed = 2;
		final int sqlFailed = 3;
		final int notExist = 4;
		final int cannotDelete = 5;

		Connection conn = DBUtil.getConn();
		
		int findResult = find(Integer.valueOf(studentId),"select * from user_info where user_id=?",true,conn);

		if(findResult == 2){
			DBUtil.closeConn();
			return notExist;
		} else if(findResult == 3){
			DBUtil.closeConn();
			return sqlFailed;
		}
		
		int rank = findRank(studentId,conn);

		if(rank < 3){

			List<Object> courseId = findAllId("select * from "+studentId+"_course","course_id",true,conn);
			if(rank == 2 && courseId.size() > 0){
				DBUtil.closeConn();
				return cannotDelete;
			}
			else{
				if(courseId.size() != 0){
					for( Object a:courseId ){
						Integer id = (Integer) a;
						int deleteStudentList = manipulateMeta(Integer.valueOf(studentId),"delete from "+id+"_student where student_id=?",true,conn);
						if(deleteStudentList == 2){
							DBUtil.closeConn();
							return deleteFailed;
						}else if(deleteStudentList == 3){
							DBUtil.closeConn();
							return sqlFailed;
						}
						int deleteCourseRecord = manipulateMeta(Integer.valueOf(studentId),"delete from "+id+"_course_record where student_id=?",true,conn);
						int newCapacity = manipulateMeta(id,"update course_info set course_Capacity = course_Capacity - 1 where course_id = ?",true,conn);
						if(newCapacity == 2){
							DBUtil.closeConn();
							return deleteFailed;
						}else if(deleteCourseRecord == 3||newCapacity == 3){
							DBUtil.closeConn();
							return sqlFailed;
						}
					}
				}
			}

			int deleteCourse = manipulateStatement("drop table if exists "+studentId+"_course",conn);
			int deleteRecord;
			if(rank == 1)
				deleteRecord = manipulateStatement("drop table if exists "+studentId+"_record",conn);
			else{
				deleteRecord = manipulateStatement("drop table if exists "+studentId+"_courseTime",conn);
			}
			if(deleteCourse == -1 || deleteRecord == -1){
				DBUtil.closeConn();
				return sqlFailed;
			}
	
		}
		
		int deleteResult = manipulateMeta(Integer.valueOf(studentId),"delete from user_info where user_id=?",true,conn);
		DBUtil.closeConn();
		if(deleteResult == 3){
			DBUtil.closeConn();
			return sqlFailed;
		}
		return deleteSuccess;
	}

	public int deleteCourse(int courseId){
		final int deleteSuccess = 1;
		final int deleteFailed = 2;
		final int sqlFailed = 3;
		final int alreadyDeleted = 4;

		Connection conn = DBUtil.getConn();

		int findResult = find(Integer.valueOf(courseId),"select * from course_info where course_id=?",true,conn);
		
		if(findResult == 2){
			DBUtil.closeConn();
			return alreadyDeleted;
		}else if(findResult == 3){
			DBUtil.closeConn();
			return sqlFailed;
		}

		List<Object> studentId = findAllId("select * from "+courseId+"_student","student_id",true,conn);

		if(!(studentId==null) && (studentId.size()>0)){
			for( Object a:studentId ){
				Integer id = (Integer)a;
				int deleteCourse = manipulateMeta(Integer.valueOf(courseId),"delete from "+id+"_course where course_id=?",true,conn);
				int deleteRecord = manipulateMeta(Integer.valueOf(courseId),"delete from "+id+"_record where course_id=?",true,conn);
				if(deleteCourse == 2){
					DBUtil.closeConn();
					return deleteFailed;
				}else if(deleteCourse == 3||deleteRecord == 3){
					DBUtil.closeConn();
					return sqlFailed;
				}
			}
		}

		int teacherId = findInt("course_TeacherId","select * from course_info where course_id ="+courseId,conn);

		int deleteTeacherResult = deleteCourseFromTeacher(courseId,teacherId,conn);
		if(deleteTeacherResult == -1){
			DBUtil.closeConn();
			return sqlFailed;
		}

		int deleteTimeTable = manipulateStatement("drop table if exists "+courseId+"_time", conn);
		int deleteStudentTable = manipulateStatement("drop table if exists "+courseId+"_student",conn);
		int deleteRecordTable = manipulateStatement("drop table if exists "+courseId+"_course_record",conn);
		if(deleteTimeTable == -1 || deleteStudentTable == -1||deleteRecordTable == -1){
			DBUtil.closeConn();
			return sqlFailed;
		}
		int deleteResult = manipulateMeta(Integer.valueOf(courseId),"delete from course_info where course_id=?",true,conn);
		DBUtil.closeConn();
		if(deleteResult == 2){
			return deleteFailed;
		}else if(deleteResult == 3){
			return sqlFailed;
		}
		return deleteSuccess;
	}

	private int updateStudentRecord( String start, int studentId, int courseId, Connection conn ){
		final int updateSuccess = 1;
		final int updateFailed = 2;
		final int sqlFailed = 3;

		ResultSet rs = null;
		String sql2 = "select * from "+studentId+"_record where course_id=? and course_time_start=? and student_assign=?";
		try(PreparedStatement ps2 = conn.prepareStatement(sql2)){
			ps2.setInt(1,courseId);
			ps2.setString(2,start);
			ps2.setInt(3,1);
			rs = ps2.executeQuery();
			if(rs.next())
				return updateFailed;
		}catch(SQLException e){
			LOGGER.error("Failed to find assign record:",e);
			return sqlFailed;
		}finally{
			closeResult(rs);
		}
		String sql = "update "+studentId+"_record set student_assign=? where course_id=? and course_time_start=?";

		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setInt(1,1);
			ps.setInt(2,courseId);
			ps.setString(3,start);

			ps.executeUpdate();
			return updateSuccess;
		}catch(SQLException e){
			LOGGER.error("Failed to update mysql record:",e);
			return sqlFailed;
		}
	}

	private int updateCourseRecord(String start, int studentId, int courseId, Connection conn){
		final int updateSuccess = 1;
		final int sqlFailed = 3;

		String sql2 = "update "+courseId+"_course_record set student_assign=? where student_id=? and course_time_start=?";

		try(PreparedStatement ps2 = conn.prepareStatement(sql2)){
			ps2.setInt(1,1);
			ps2.setInt(2,studentId);
			ps2.setString(3,start);

			ps2.executeUpdate();
			return updateSuccess;
		}catch(SQLException e){
			LOGGER.error("Failed to update mysql record:",e);
			return sqlFailed;
		}
	}

	public String assignForCourse( int studentId, String time ){//学生登录

		JSONObject json = new JSONObject();

		Connection conn = DBUtil.getConn();

		int rank = findRank(studentId,conn);

		String sql = "select * from "+studentId+"_record where course_time_start <= ? and course_time_end >= ?";
		
		ResultSet rs = null;
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1,time);
			ps.setString(2,time);

			rs = ps.executeQuery();
			if(!rs.next()){
				json.put(message,"签到错误\n不在签到时间内");
			}
			else{
				String start= rs.getString("course_time_start");
				int id= rs.getInt("course_id");
				int result = updateStudentRecord(start, studentId, id , conn );
				if( result == 2 ){
					json.put(message,"您已经签到过了");
				}else if(result == 3 ){
					json.put(message,"签到错误\n数据库错误");	
				}else{
					int result2 = updateCourseRecord(start, studentId, id, conn);
					int result3 = manipulateMeta( Integer.valueOf(studentId),"update "+id+"_student set student_assign = student_assign + 1 where student_id = ?",true,conn);
					if( result2 == 3 || result3 == 3 ){
						json.put(message,"签到错误\n数据库错误");
					}else{
						getMetaInfo(rs,json);
						json.put("student_assign","1");
					}
				}
			}
		}catch(SQLException e){
			LOGGER.error("Failed to select from sql",e);
			json.put(message,"签到错误\n数据库错误");
			
		}finally{
			DBUtil.closeConn();
			closeResult(rs);
		}
		return json.toString();
	}

	public String getAll(String sql, boolean oneParameter, String name){// 所有搜寻到的信息
		JSONObject json = new JSONObject();
		JSONArray arr = new JSONArray();

		Connection conn = DBUtil.getConn();
		ResultSet rs = null;

		boolean has = false;

		try(PreparedStatement ps = conn.prepareStatement(sql)){
			if(oneParameter)
				ps.setString(1,name);
			rs = ps.executeQuery();
			while(rs.next()){
				has = true;
				JSONObject tmp = new JSONObject();
				getMetaInfo(rs,tmp);
				arr.add(tmp);
			}
			if(has == true){
				json.put(message,"获取成功");
				json.put("contents",arr);
			}
			else{
				json.put(message,"不存在记录");
			}
		}catch(SQLException e){
			LOGGER.error("Failed to get info when parsing "+sql,e);
			json.put(message,"获取错误\n数据库错误");
			
		}finally{
			DBUtil.closeConn();
			closeResult(rs);
			}
		
		return json.toString();
	}

	public String getAllWithCheck(){// 所有搜寻到的信息
		
		JSONObject json = new JSONObject();
		JSONArray arr = new JSONArray();

		Connection conn = DBUtil.getConn();
		ResultSet rs = null;

		boolean has = false;

		try(PreparedStatement ps = conn.prepareStatement( "select * from user_info")){
			
			rs = ps.executeQuery();
			while(rs.next()){
				has = true;
				if(rs.getInt("user_Rank") < 3){
					JSONObject tmp = new JSONObject();
					getMetaInfo(rs,tmp);
					arr.add(tmp);
					has = true;
				}
			}
			if(has){
				json.put(message,"获取成功");
				json.put("contents",arr);
			}
			else{
				json.put(message,"不存在记录");
			}
		}catch(SQLException e){
			LOGGER.error("Failed to get all user info",e);
			json.put(message,"获取错误\n数据库错误");
			
		}finally{
			DBUtil.closeConn();
			closeResult(rs);
			}
		return json.toString();
	}

	public String getAllStudent( int courseId ){//课程的全部签到情况
		return getAll("select * from "+courseId+"_student",false,null);
	}
	public String getRecordByCourse( int courseId ){
		return getAll("select * from "+courseId+"_course_record",false,null);
	}
	public String getRecordByStudent( int studentId ){
		return getAll("select * from "+studentId+"_record", false, null);
	}

	public String getNotAssignedStudent( int courseId, String start ){//是否在别的课内?
		return getAll("select * from "+courseId+"_course_record where course_time_start = ? and student_assign=0",true,start);
	}

	public String getAssignedClassRecord( int studentId, int courseId){//是否在这节课内?
		String time = getCurrentTime();
		return getAll("select * from "+studentId+"_record where course_id="+courseId+" and course_time_start <= ?",true,time);
	}

	public String getNowClass( int teacherId ){

		String nowTime = getCurrentTime();
		
		Connection conn = DBUtil.getConn();

		JSONObject json = new JSONObject();

		String sql = "select * from "+teacherId+"_courseTime where course_time_start <= ? and course_time_end >=?";

		ResultSet rs = null;

		try( PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1,nowTime);
			ps.setString(2,nowTime);
			
			rs = ps.executeQuery();
			if(rs.next()){
				getMetaInfo(rs,json);
				json.put(message,"获取成功");
			}else{
				json.put(message,"获取失败\n不存在该课");
			}
		}catch(SQLException e){
			LOGGER.error("Failed to get class",e);
			json.put(message,"获取失败\n数据库错误");
		}finally{
			DBUtil.closeConn();
			closeResult(rs);
		}
		return json.toString();
	}

	public String getCourseTime(int classId){
		return getAll("select * from "+classId+"_time",false, null);
	}

	public String getCourseInfo(int courseId){

		JSONObject back = new JSONObject();

		Connection conn = DBUtil.getConn();

		String str = "select * from course_info where course_id="+courseId;

		ResultSet rs = null;

		try(PreparedStatement ps = conn.prepareStatement(str)){
			rs = ps.executeQuery();
			if(rs.next()){
				getMetaInfo(rs,back);
			}else{
				back.put(message,"无此课程");
				return back.toString();
			}
		}catch(SQLException e){
			LOGGER.error("Error when getting course info",e);
			back.put(message,"数据库错误");
			return back.toString();
		}finally{
			DBUtil.closeConn();
			closeResult(rs);
		}
		str = getCourseTime(courseId);
		JSONObject json2 = JSONObject.parseObject(str);
		String signal = json2.getString(message);
		if(!signal.equals("获取成功") && !signal.equals("不存在记录")){
			back.put(message,"获取课程时间失败");
			return back.toString();
		}
		back.put("classtime",json2.getJSONArray("contents"));
		return back.toString();
	}

	public String getCourseAll(int studentId){//从一个学生或者一个老师得到所有的课程
		
		JSONObject jsonBack = new JSONObject();
		JSONArray arr = new JSONArray();
		Connection conn = DBUtil.getConn();
		List<Object> courseId = findAllId("select * from "+studentId+"_course","course_id",true,conn);
		DBUtil.closeConn();
		for(Object obj:courseId){
			int course = (Integer)obj;
			String str = getCourseInfo(course);
			JSONObject json = JSONObject.parseObject(str);
			String signal = json.getString(message);//
			if(!signal.equals("成功")){
				jsonBack.put(message,"获取课程失败");
				return jsonBack.toString();
			}
			arr.add(json);
		}
		jsonBack.put("contents",arr);
		jsonBack.put(message,"成功");
		return jsonBack.toString();
	}

	public int checkValidStuCourse(int studentId, int teacherId, int courseId, String time){

		final int passed = 1;
		final int teacherCourseFailed = 2;
		final int courseTimeFailed = 3;
		final int courseStudentFailed = 4;

		Connection conn = DBUtil.getConn();
		int findCourseToTeacher = find(Integer.valueOf(courseId),"select * from "+teacherId+"_course where course_id = ?",true,conn);
		if(findCourseToTeacher != 1){
			DBUtil.closeConn();
			return teacherCourseFailed;
		}
		int findTimetoCourse = find(time,"select * from "+courseId+"_time where course_time_start=?",false,conn);
		if(findTimetoCourse != 1){
			DBUtil.closeConn();
			return courseTimeFailed;
		}
		int findStudentToCourse = find(Integer.valueOf(studentId),"select * from "+courseId+"_student where student_id=?",true,conn);
		DBUtil.closeConn();
		if(findStudentToCourse != 1){
			return courseStudentFailed;
		}return passed;
	}
}