package com.onthedeer.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

import java.net.URLDecoder;
 
/**
 * Servlet implementation class UserServlet
 * 作为服务器的交互部分存在
 */

@WebServlet("/UserServlet")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String encodingMethod = "utf-8";
	private static final String jsonError = "JSON参数错误";
	private static final String studentMark = "studentId";
	private static final String courseMark = "courseId";
	UserService userService = new UserService();
	private Map<Integer,String> tokenMap = new HashMap<>();//除了login信息之外，其它的信息都需要含有userId和token
	private Map<Integer,Integer> typeMap = new HashMap<>();//对于不同的人需要有不同的type
	private Map<Integer,Long> timeMap = new HashMap<>();
	private String type;

	private JSONObject jsonBack = new JSONObject();

	private String sign = "message";

	private boolean timeCheck(Integer userId){
		if(!timeMap.containsKey(userId))
			return false;
		else if( ( System.currentTimeMillis() - timeMap.get(userId) ) > 600000){
			deleteRecord(userId);
			jsonBack.put(sign,"未操作时间太长，您已自动下线");
			return false;
		}
		return true;
	}

	private void deleteRecord(Integer userId){
		tokenMap.remove(userId);
		typeMap.remove(userId);
		timeMap.remove(userId);
	}

	private boolean checkToken( Integer userId, String token){
		if(tokenMap.containsKey( userId ) && (tokenMap.get(userId)).equals(token) && (!type.equals("login")))
			return true;
		else{
			if(token.equals("1001") && type.equals("login"))
				return true;
			else
				return false;
		}
	}

	private boolean checkType(int type, Integer userId){
		if(!typeMap.containsKey( userId ) || !(typeMap.get(userId) == type )){
			return false;
		}return true;
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");

		StringBuilder buf = new StringBuilder();
		String line = null;
		jsonBack = new JSONObject();
		try {
			BufferedReader reader = request.getReader();
			while((line = reader.readLine()) != null)
				buf.append(line);
		} catch(Exception e) {
			jsonBack.put(sign,"服务器解析错误...\n未得到指令内容");
			response.getOutputStream().write((jsonBack.toString()).getBytes(encodingMethod));
			return;
		}
		try {
			JSONObject jsonObject = JSONObject.parseObject( buf.toString() );
			type = jsonObject.getString("type");
			Integer userId = jsonObject.getInteger("userId");
			String token = jsonObject.getString("token"); //除了login的初始token为1001之外，其余的token均为随机分配i99
			if(type == null || userId == null || token == null){
				jsonBack.put(sign,"服务器解析错误...\n核心参数缺失");
				return;
			}
			if(checkToken(userId,token) == false){
				jsonBack.put(sign,"操作不被允许：\nToken验证不正确，或者是有人在别处登录");
				return;
			}
			if(!type.equals("login") && !timeCheck(userId)){
					jsonBack.put(sign,"登录已过期");
					return;
			}
			if(!type.equals("logout")){
				timeMap.put(userId,System.currentTimeMillis());
			}
			int result ,studentId, courseId;

			if(type.equals("login")){
				String password = jsonObject.getString("password");
				if( password == null ){
					jsonBack.put(sign,"登录失败...\n参数错误或缺失");
					return;
				}
				String back;
				back = userService.login(userId.intValue(), password);
				jsonBack = JSONObject.parseObject(back);
				String msg;
				msg = jsonBack.getString(sign);
				if( msg != null && msg.equals("成功") ) {
					Integer userrank = Integer.valueOf(jsonBack.getString("user_Rank"));
					String uuid;
					do{
						uuid = (UUID.randomUUID()).toString();
					}while(tokenMap.containsValue(uuid));
					tokenMap.put(userId,uuid);
					typeMap.put(userId,userrank);
					jsonBack.put("newToken",uuid);
				}
			}else if(type.equals("registerUser")){
				int rank = jsonObject.getIntValue("maxrank");
				if(rank == 0){
					jsonBack.put(sign,jsonError);
					return;
				}
				if( ( rank < 3 && checkType(3,userId) == false ) 
					|| ( rank == 3 && checkType(4,userId) == false ) || rank > 3
				){
					jsonBack.put(sign,"注册失败...\n您无权限注册这些用户");
					return;
				}
				String back;
				back = userService.registerBatchUser(jsonObject);
				jsonBack = JSONObject.parseObject(back);
			}else if(type.equals("registerCourse")){
				if(checkType(3,userId) == false){
					jsonBack.put(sign,"添加课程失败...\n您无权限注册课程");
					return;
				}
				String back;
				back = userService.registerBatchCourse(jsonObject);
				jsonBack = JSONObject.parseObject(back);
			}else if(type.equals("getCourseInfo")){
				courseId = jsonObject.getIntValue("id");
				if( courseId == 0 ){
					jsonBack.put(sign,jsonError);
					return;
				}
				String back = userService.getCourseInfo(courseId);
				jsonBack = JSONObject.parseObject(back);
			}else if(type.equals("modifyUserInfo")){
				int modifiedId;
				modifiedId = jsonObject.getIntValue("id");
				if(modifiedId == 0){
					jsonBack.put(sign,jsonError);
					return;
				}
				Connection conn1 = DBUtil.getConn();
				int userRank = userService.findRank(modifiedId,conn1);
				DBUtil.closeConn();
				if( ( checkType(3,userId)== false || userRank >= 3 ) && (modifiedId != userId) ){
						jsonBack.put(sign,"修改信息失败...\n您无权限修改用户信息");
						return;
				}
				result = userService.modifyUserInfo(jsonObject);
				ModifyJson.switchCase(result,1,jsonBack);
			}else if(type.equals("modifyCourseInfo")){
				courseId = jsonObject.getIntValue("id");
				if(courseId == 0){
					jsonBack.put(sign,jsonError);
					return;
				}
				if(checkType(3,userId) == false){
					if(checkType(2,userId) == true){
						Connection conn0 = DBUtil.getConn();
						int tempFind = userService.find(Integer.valueOf(courseId),"select * from "+userId+"_course where course_id=?",true,conn0);
						DBUtil.closeConn();
						if(  tempFind != 1) {
							jsonBack.put(sign,"修改课程信息失败...\n您无权限修改课程信息");
							return;
						}
					}else{
						jsonBack.put(sign,"修改课程信息失败...\n您无权限修改课程信息");
						return;
					}
				}
				
				result = userService.modifyCourseInfo(jsonObject);
				ModifyJson.switchCase(result,2,jsonBack);
			}else if(type.equals("setCourseTime")){
				if(checkType(3,userId) == false){
					jsonBack.put(sign,"设置课时失败...\n您无此权限");
					return;
				}
				result = userService.modifyTime(jsonObject);
				
				ModifyJson.switchCase(result,3,jsonBack);
			}else if(type.equals("registerStudent2Course") ||type.equals("deleteStudent2Course") ){
				boolean reg = type.equals("registerStudent2Course");
				int out = 0;
				String san = "";
				if(reg){
					out = 4;
					san = "添加到";
				}else{
					out = 5;
					san = "移除出";
				}
				if(checkType(3,userId) == false){
					jsonBack.put(sign,"失败...\n您无权限将学生"+san+"课程");
					return;
				}
				studentId = jsonObject.getIntValue(studentMark);
				courseId = jsonObject.getIntValue(courseMark);
				if( studentId * courseId == 0 ){
					jsonBack.put(sign,jsonError);
					return;
				}
				result = 0;
				if(reg){
					result = userService.registerStu2Course(studentId,courseId);
				}else{
					result = userService.deleteStu2Course(studentId,courseId);
				}
				ModifyJson.switchCase(result,out,jsonBack);
			}else if(type.equals("assign")){
				if(checkType(1,userId) == false){
					jsonBack.put(sign,"签到失败...\n您不是学生");
					return;
				}
				String time;
				time = userService.getCurrentTime();
				String info;
				info = userService.assignForCourse(userId,time);
				jsonBack = JSONObject.parseObject(info);
			}else if(type.equals("teacherAddAssign")){
				studentId = jsonObject.getIntValue(studentMark);
				courseId = jsonObject.getIntValue(courseMark);
				String time;
				time = jsonObject.getString("registerTime");
				if( studentId == 0 || courseId == 0 || time == null){
					jsonBack.put(sign,jsonError);
					return;
				}
				boolean dataFmt;
				dataFmt = ModifyJson.checkTimeFormat(time);
				if(dataFmt == false){
					jsonBack.put(sign,"日期格式错误\n应为'yyyy-MM-dd HH:mm:ss'式合法日期");
					return;
				}
				if(checkType(2,userId) == false){
					jsonBack.put(sign,"补签到失败...\n您不是教师");
					return;
				}
				int checkResult = userService.checkValidStuCourse( studentId, userId, courseId, time );
				if(checkResult != 1){
					jsonBack.put(sign,"检验错误");
					return;
				}
				String info;
				info = userService.assignForCourse(studentId,time);
				jsonBack = JSONObject.parseObject(info);
			}else if(type.equals("deleteCourse")){
				if(checkType(3,userId) == false){
					jsonBack.put(sign,"删除失败...\n您无权限删除课程");
					return;
				}
				courseId = jsonObject.getIntValue(courseMark);
				if(courseId == 0){
					jsonBack.put(sign,jsonError);
					return;
				}
				result = userService.deleteCourse(courseId);
				
				ModifyJson.switchCase(result,6,jsonBack);
			}else if(type.equals("deleteUser")){
				int deleteId;
				deleteId = jsonObject.getIntValue("deleteId");
				if(deleteId == 0){
					jsonBack.put(sign,jsonError);
					return;
				}
				Connection conn = DBUtil.getConn();
				int userRank = userService.findRank(deleteId,conn);
				DBUtil.closeConn();
				if( userRank == 0){
					jsonBack.put(sign,"不存在此用户");
					return;
				}
				if( ( userRank < 3 && checkType(3,userId) == false ) || ( userRank == 3 && checkType(4,userId) == false ) || userRank > 3 ){
					jsonBack.put(sign,"删除失败...\n您无权限删除用户");
					return;
				}
				result = userService.deleteStudent( deleteId );
				
				ModifyJson.switchCase(result,7,jsonBack);
			}else if(type.equals("getAllCourses")){
				String classInfo;
				classInfo = userService.getAll("select * from course_info",false,null);
				jsonBack = JSONObject.parseObject(classInfo);
			}else if(type.equals("getAllUsers")){
				if(checkType(3,userId) == false){
					jsonBack.put(sign,"获取用户失败...\n您无权限得到所有用户");
					return;
				}
				String userInfo;
				userInfo = userService.getAllWithCheck();
				jsonBack = JSONObject.parseObject(userInfo);
			}else if(type.equals("getRecordByStudent")){
				studentId = jsonObject.getIntValue(studentMark);
				if(studentId == 0){
					jsonBack.put(sign,jsonError);
					return;
				}
				if(checkType(3,userId) == false && userId != studentId){
					jsonBack.put(sign,"获取用户记录失败...\n您无权限得到该签到记录");
					return;
				}
				Connection conn = DBUtil.getConn();
				int rank;
				rank = userService.findRank(studentId,conn);
				DBUtil.closeConn();
				if(rank != 1){
					jsonBack.put(sign,"错误\n不是学生");
					return;
				}
				String recordInfo;
				recordInfo = userService.getRecordByStudent(studentId);
				jsonBack = JSONObject.parseObject(recordInfo);
			}else if(type.equals("getRecordByCourse") || type.equals("getAllStudentByCourse")){
				boolean isRecord;
				isRecord = type.equals("getRecordByCourse");
				String param = "";
				if(isRecord)
					param = "获取课程记录失败...\n您无权限得到该签到记录";
				else
					param = "获取学生失败...\n课程不存在或您无权限得到该课程的学生";
				courseId = jsonObject.getIntValue(courseMark);
				if(courseId == 0){
					jsonBack.put(sign,jsonError);
					return;
				}
				if(checkType(3,userId) == false){
					Connection conn = DBUtil.getConn();
					if(checkType(2,userId) == false || userService.find(Integer.valueOf(courseId),"select * from "+userId+"_course where course_id=?",true ,conn) != 1 ){
						DBUtil.closeConn();
						jsonBack.put(sign,param);
						return;
					}
					DBUtil.closeConn();
				}
				String got = "";
				if(isRecord)
					got = userService.getRecordByCourse(courseId);
				else
					got = userService.getAllStudent(courseId);
				jsonBack = JSONObject.parseObject(got);
			}else if(type.equals("getAssignClassRecord")){
				courseId = jsonObject.getIntValue(courseMark);
				studentId = jsonObject.getIntValue(studentMark);
				if( courseId * studentId == 0 ){
					jsonBack.put(sign,jsonError);
					return;
				}
				if(checkType(3,userId) == false){
					Connection conn = DBUtil.getConn();
					if( ( checkType(2,userId) == false || userService.find( Integer.valueOf(courseId),"select * from "+userId+"_course where course_id=?",true,conn) != 1) 
						&& (checkType(1,userId) == false || userId != studentId)){
						DBUtil.closeConn();
						jsonBack.put(sign,"获取签到记录失败...\n您无权限得到该学生的签到记录");
						return;
					}
					DBUtil.closeConn();
				}
				String assignGot;
				assignGot = userService.getAssignedClassRecord(studentId,courseId);
				jsonBack = JSONObject.parseObject(assignGot);

			}else if(type.equals("getNotAssignedStudent")){
				courseId = jsonObject.getIntValue(courseMark);
				String courseStart;
				courseStart = jsonObject.getString("start_time");
				if( courseId == 0 || courseStart == null){
					jsonBack.put(sign,jsonError);
					return;
				}
				boolean dataFmt;
				dataFmt = ModifyJson.checkTimeFormat(courseStart);
				if(dataFmt == false){
					jsonBack.put(sign,"日期格式错误\n应为'yyyy-MM-dd HH:mm:ss'式合法日期");
					return;
				}
				if(checkType(3,userId) == false){
					Connection conn = DBUtil.getConn();
					if(checkType(2,userId) == false || userService.find(Integer.valueOf(courseId),"select * from "+userId+"_course where course_id=?",true, conn) != 1 ){
						DBUtil.closeConn();
						jsonBack.put(sign,"获取签到记录失败...\n您无权限得到该课程的签到记录");
						return;
					}
					DBUtil.closeConn();
				}
				String classAssignGot;
				classAssignGot = userService.getNotAssignedStudent(courseId,courseStart);
				jsonBack = JSONObject.parseObject(classAssignGot);
			}else if(type.equals("getOngoingClass")){
				if(checkType(2,userId)==false){
					jsonBack.put(sign,"您不是教师，无权限得到正在进行的课程");
					return;
				}
				String ongoingClass;
				ongoingClass = userService.getNowClass(userId);
				jsonBack = JSONObject.parseObject(ongoingClass);
			}else if(type.equals("logout")){
				deleteRecord(userId);
				jsonBack.put(sign,"已登出");

			}else if( type.equals("getCourseTime")){
				courseId = jsonObject.getIntValue(courseMark);
				if(courseId == 0){
					jsonBack.put(sign,jsonError);
					return;
				}
				String courseTime;
				courseTime = userService.getCourseTime(courseId);
				jsonBack = JSONObject.parseObject(courseTime);
			}else if( type.equals("getEveryCourseInfo")){
				studentId = jsonObject.getIntValue("getId");
				if(studentId == 0){
					jsonBack.put(sign,jsonError);
					return;
				}
				if( checkType(3,userId) == false && (studentId != userId) ){
					jsonBack.put(sign,"获取课程失败...\n您无权限得到该用户所有课程");
					return;
				}
				String courseAllInfo;
				courseAllInfo = userService.getCourseAll(studentId);
				jsonBack = JSONObject.parseObject(courseAllInfo);
			}
			else{
				jsonBack.put(sign,"指令错误");
			}			
		}catch(Exception e) {
			jsonBack.put(sign,"服务器解析错误...\n未能成功解析成为Json");
		}finally{
			response.getOutputStream().write((URLDecoder.decode(jsonBack.toString(),encodingMethod)).getBytes(encodingMethod));
		}
	}
}