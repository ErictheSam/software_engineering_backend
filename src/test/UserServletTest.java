package test;
import com.onthedeer.server.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;
 
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;

public class UserServletTest extends TestCase{
	
	private UserServlet userServlet;

    private UserService userService;
	
	private MockHttpServletRequest request;
	
	private MockHttpServletResponse response;
	
	private JSONObject jsonObject;

	public UserServletTest(String testName)
	{
		super(testName);
		userServlet = new UserServlet();
        userService = new UserService();
	    jsonObject = new JSONObject();

	
	}
	
	public static Test suite()
	{
		return new TestSuite( UserServletTest.class);
	}
	
	public void testAll() throws Exception{
	
		request = new MockHttpServletRequest(){
			public BufferedReader getReader(){
				byte[] by = (jsonObject.toString()).getBytes();
		
				InputStream is = new ByteArrayInputStream(by);
		
				InputStreamReader isr = 
				new InputStreamReader(is);
		
				BufferedReader reader = new BufferedReader(isr);
				return reader;
			}
		};
		
		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		JSONObject jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("服务器解析错误...\n核心参数缺失",jsonBack.getString("message"));

		jsonObject.put("type","login");
		jsonObject.put("userId",1);
		jsonObject.put("token","1001");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("登录失败...\n参数错误或缺失",jsonBack.getString("message"));

		jsonObject.put("password","root");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));
		assertEquals("4",jsonBack.getString("user_Rank"));

		String token = jsonBack.getString("newToken");

		jsonObject.put("token",token);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("操作不被允许：\nToken验证不正确，或者是有人在别处登录",jsonBack.getString("message"));

		jsonObject.put("userId",2);
		jsonObject.put("token","1001");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("登录失败...\n请检查学号，用户名和密码是否正确",jsonBack.getString("message"));

		jsonObject.put("userId",1);
		jsonObject.put("type","registerUser");
		jsonObject.put("token",token);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("maxrank",3);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		JSONObject json2 = new JSONObject();
		JSONArray arr = new JSONArray();
		json2.put("id",1024);
		json2.put("username","admin");
		json2.put("password","admin");
		json2.put("gender",2);
		json2.put("department","admin");
		json2.put("birthday","1999-01-01");
		arr.add(json2);
		jsonObject.put("content",arr);
		jsonObject.put("maxrank",3);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("第1个用户JSON参数错误",jsonBack.getString("message"));

		json2.put("rank",4);

		arr = new JSONArray();
		arr.add(json2);
		jsonObject.put("content",arr);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("注册失败...\n您无权限注册第1个用户",jsonBack.getString("message"));

		json2.put("rank",3);

		arr = new JSONArray();
		arr.add(json2);
		jsonObject.put("content",arr);
		jsonObject.put("maxrank",4);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("注册失败...\n您无权限注册这些用户",jsonBack.getString("message"));

		jsonObject.put("maxrank",3);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("注册成功",jsonBack.getString("message"));

		jsonObject.put("type","1");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("指令错误",jsonBack.getString("message"));

		jsonObject.put("type","registerUser");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("第1个用户已存在",jsonBack.getString("message"));

		jsonObject.put("type","logout");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("已登出",jsonBack.getString("message"));

		jsonObject.put("type","registerUser");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("操作不被允许：\nToken验证不正确，或者是有人在别处登录",jsonBack.getString("message"));

		jsonObject = new JSONObject();

		jsonObject.put("type","login");
		jsonObject.put("userId",1024);
		jsonObject.put("token","1001");
		jsonObject.put("password","admin");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));

		token = jsonBack.getString("newToken");

		json2 = new JSONObject();

		jsonObject.put("type","registerUser");
		jsonObject.put("token",token);

		json2.put("id",10001);
		json2.put("username","student");
		json2.put("password","student");
		json2.put("gender",1);
		json2.put("department","Computer Science");
		json2.put("birthday","1999-10-10");
		json2.put("rank",1);

		JSONObject json3 = new JSONObject();

		json3.put("id",20001);
		json3.put("username","鹿老师");
		json3.put("password","teacher");
		json3.put("gender",1);
		json3.put("department","Computer Science");
		json3.put("birthday","1999-10-10");
		json3.put("rank",2);

		JSONObject json4 = new JSONObject();

		json4.put("id",10002);
		json4.put("username","student2");
		json4.put("password","student2");
		json4.put("gender",1);
		json4.put("department","Computer Science");
		json4.put("birthday","1999-10-10");
		json4.put("rank",1);

		JSONObject json5 = new JSONObject();

		json5.put("id",20002);
		json5.put("username","teacher2");
		json5.put("password","teacher2");
		json5.put("gender",1);
		json5.put("department","Computer Science");
		json5.put("birthday","1999-10-10");
		json5.put("rank",2);

		JSONObject json6 = new JSONObject();

		json6.put("id",10003);
		json6.put("username","student3");
		json6.put("password","student3");
		json6.put("gender",1);
		json6.put("department","Computer Science");
		json6.put("birthday","1999-10-10");
		json6.put("rank",1);

		JSONObject json7 = new JSONObject();

		json7.put("id",10004);
		json7.put("username","student4");
		json7.put("password","student4");
		json7.put("gender",1);
		json7.put("department","Computer Science");
		json7.put("birthday","1999-10-10");
		json7.put("rank",1);
		
		arr = new JSONArray();

		arr.add(json2);
		arr.add(json3);
		arr.add(json4);
		arr.add(json5);
		arr.add(json6);
		arr.add(json7);

		jsonObject.put("content",arr);
		jsonObject.put("maxrank",2);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("注册成功",jsonBack.getString("message"));

		jsonObject.put("type","registerCourse");

		jsonObject.remove("content");
		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		json2 = new JSONObject();

		arr = new JSONArray();

		json2.put("id",12);
		
		arr.add(json2);

		jsonObject.put("content",arr);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("第1门课JSON参数错误",jsonBack.getString("message"));

		JSONArray jsonTemp = new JSONArray();
		JSONObject jsonstu = new JSONObject();
		JSONObject jsonstu2 = new JSONObject();

		jsonstu.put("studentId",10001);
		jsonstu2.put("studentId",10003);

		jsonTemp.add(jsonstu);
		jsonTemp.add(jsonstu2);

		json2.put("TeacherId",1023);
		json2.put("room","010");
		json2.put("coursename","CSAPP");
		json2.put("students",jsonTemp);

		arr = new JSONArray();
		arr.add(json2);
		jsonObject.put("content",arr);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("第1门课教师不存在",jsonBack.getString("message"));

		json2.put("TeacherId",20002);

		arr = new JSONArray();
		arr.add(json2);
		jsonObject.put("content",arr);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("注册成功",jsonBack.getString("message"));

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("第1门课已存在",jsonBack.getString("message"));

		jsonObject.put("type","registerStudent2Course");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("studentId",1023);
		jsonObject.put("courseId",12);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("不存在学生或课程",jsonBack.getString("message"));

		jsonObject.put("studentId",10001);
		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("已经添加过了",jsonBack.getString("message"));

		arr = new JSONArray();
		
		for(int i = 1; i <= 5; i ++){
			JSONObject class_time = new JSONObject();
			class_time.put("start_time","2019-11-2"+i+" 16:00:00");
			class_time.put("end_time","2019-11-2"+i+" 23:59:59");
			arr.add(class_time);
		}

		assertEquals(5,arr.size());

		jsonObject.put("classtime",arr);
		jsonObject.put("type","setCourseTime");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("id",13);

		jsonObject.put("modifyType",1);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("设置课时失败，课程不存在",jsonBack.getString("message"));

		jsonObject.put("id",12);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("设置课时成功",jsonBack.getString("message"));

		jsonObject.put("type","registerStudent2Course");

		jsonObject.put("studentId",10002);
		jsonObject.put("courseId",12);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功添加",jsonBack.getString("message"));

		jsonObject.put("type","modifyUserInfo");

		jsonObject.put("id",10001);
		jsonObject.put("password","jysks");
		jsonObject.put("department","Economics and Management");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("birthday","2000-01-01");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("修改用户信息成功",jsonBack.getString("message"));

		jsonObject.put("type","modifyCourseInfo");

		jsonObject.put("id",12);
		jsonObject.put("TeacherId",20000);
		jsonObject.put("capacity",20);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("room","201");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("教师不存在",jsonBack.getString("message"));

		jsonObject.put("TeacherId",20001);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("修改课程信息成功",jsonBack.getString("message"));

		jsonObject.put("type","assign");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("签到失败...\n您不是学生",jsonBack.getString("message"));

		jsonObject.put("type","logout");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("已登出",jsonBack.getString("message"));

		jsonObject = new JSONObject();

		jsonObject.put("type","login");
		jsonObject.put("userId",10004);
		jsonObject.put("token","1001");
		jsonObject.put("password","student4");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));

		token = jsonBack.getString("newToken");
		
		jsonObject.put("token",token);

		jsonObject.put("type","assign");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("签到错误\n不在签到时间内",jsonBack.getString("message"));

		jsonObject.put("type","logout");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("已登出",jsonBack.getString("message"));

		jsonObject = new JSONObject();

		jsonObject.put("type","login");
		jsonObject.put("userId",20002);
		jsonObject.put("token","1001");
		jsonObject.put("password","teacher2");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));

		token = jsonBack.getString("newToken");
		
		jsonObject.put("token",token);

		jsonObject.put("type","getOngoingClass");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取失败\n不存在该课",jsonBack.getString("message"));

		jsonObject = new JSONObject();

		jsonObject.put("type","login");
		jsonObject.put("userId",10001);
		jsonObject.put("token","1001");
		jsonObject.put("password","jysks");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));

		token = jsonBack.getString("newToken");
		
		jsonObject.put("token",token);

		jsonObject.put("type","getAllUsers");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取用户失败...\n您无权限得到所有用户",jsonBack.getString("message"));

		jsonObject.put("type","registerCourse");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("添加课程失败...\n您无权限注册课程",jsonBack.getString("message"));

		jsonObject.put("type","modifyUserInfo");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("id",10002);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("修改信息失败...\n您无权限修改用户信息",jsonBack.getString("message"));

		jsonObject.put("type","modifyCourseInfo");

		jsonObject.remove("id");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("id",12);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("修改课程信息失败...\n您无权限修改课程信息",jsonBack.getString("message"));

		jsonObject.put("type","setCourseTime");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("设置课时失败...\n您无此权限",jsonBack.getString("message"));

		jsonObject.put("type","registerStudent2Course");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("失败...\n您无权限将学生添加到课程",jsonBack.getString("message"));

		jsonObject.put("type","teacherAddAssign");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("studentId",10001);
		jsonObject.put("courseId",12);
		jsonObject.put("registerTime","123");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("日期格式错误\n应为'yyyy-MM-dd HH:mm:ss'式合法日期",jsonBack.getString("message"));

		jsonObject.put("registerTime","2018-01-05 10:12:00");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("补签到失败...\n您不是教师",jsonBack.getString("message"));

		jsonObject.put("type","deleteCourse");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("删除失败...\n您无权限删除课程",jsonBack.getString("message"));

		jsonObject.put("type","getRecordByStudent");

		jsonObject.put("studentId",10002);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取用户记录失败...\n您无权限得到该签到记录",jsonBack.getString("message"));

		jsonObject.put("type","getRecordByCourse");

		jsonObject.put("courseId",12);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取课程记录失败...\n您无权限得到该签到记录",jsonBack.getString("message"));

		jsonObject.put("type","getEveryCourseInfo");

		jsonObject.remove("getId");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("getId",10002);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取课程失败...\n您无权限得到该用户所有课程",jsonBack.getString("message"));

		jsonObject.put("type","getAssignClassRecord");
		jsonObject.remove("courseId");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("courseId",12);
		jsonObject.put("studentId",10002);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取签到记录失败...\n您无权限得到该学生的签到记录",jsonBack.getString("message"));

		jsonObject.put("type","getNotAssignedStudent");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("start_time","123");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("日期格式错误\n应为'yyyy-MM-dd HH:mm:ss'式合法日期",jsonBack.getString("message"));

		jsonObject.put("start_time","2019-11-22 16:00:00");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取签到记录失败...\n您无权限得到该课程的签到记录",jsonBack.getString("message"));

		jsonObject.put("type","getOngoingClass");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("您不是教师，无权限得到正在进行的课程",jsonBack.getString("message"));

		jsonObject.put("type","assign");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));
		assertEquals("1",jsonBack.getString("student_assign"));

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("您已经签到过了",jsonBack.getString("message"));

		jsonObject.put("type","logout");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("已登出",jsonBack.getString("message"));

		jsonObject = new JSONObject();

		jsonObject.put("type","login");
		jsonObject.put("userId",20001);
		jsonObject.put("token","1001");
		jsonObject.put("password","teacher");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));

		token = jsonBack.getString("newToken");

		jsonObject.put("token",token);

		jsonObject.remove("registerTime");

		jsonObject.put("type","modifyCourseInfo");

		jsonObject.put("id",101);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("修改课程信息失败...\n您无权限修改课程信息",jsonBack.getString("message"));

		jsonObject.remove("id");

		jsonObject.put("type","getCourseTime");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("courseId",12);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取成功",jsonBack.getString("message"));

		jsonObject.remove("courseId");

		jsonObject.put("type","getCourseInfo");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("id",13);
		
		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("无此课程",jsonBack.getString("message"));

		jsonObject.put("id",12);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));
		assertEquals("3",jsonBack.getString("course_Capacity"));

		jsonObject.put("type","teacherAddAssign");// 给老师开新的课程

		jsonObject.put("studentId",10002);
		jsonObject.put("courseId",12);
		jsonObject.put("registerTime","2019-12-31 16:00:00");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("检验错误",jsonBack.getString("message"));

		jsonObject.put("registerTime","2019-11-21 16:00:00");

		jsonObject.put("courseId",13);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("检验错误",jsonBack.getString("message"));

		jsonObject.put("courseId",12);

		jsonObject.put("studentId",10004);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("检验错误",jsonBack.getString("message"));

		jsonObject.put("studentId",10002);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));

		jsonObject.put("type","getEveryCourseInfo");
		jsonObject.put("getId",10002);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取课程失败...\n您无权限得到该用户所有课程",jsonBack.getString("message"));

		jsonObject.put("getId",20001);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));

		JSONArray jsonarr = jsonBack.getJSONArray("contents");

		assertEquals(1,jsonarr.size());

		jsonObject.put("type","getAllStudentByCourse");
		jsonObject.put("courseId",12);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取成功",jsonBack.getString("message"));

		jsonarr = jsonBack.getJSONArray("contents");

		assertEquals(3,jsonarr.size());

		jsonObject.put("type","getAssignClassRecord");

		jsonObject.put("studentId",10001);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取成功",jsonBack.getString("message"));

		jsonarr = jsonBack.getJSONArray("contents");

		assertEquals(2,jsonarr.size());

		jsonObject.put("type","getOngoingClass");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取成功",jsonBack.getString("message"));

		String courseStart = jsonBack.getString("course_time_start");

		jsonObject.put("type","getNotAssignedStudent");

		jsonObject.put("start_time",courseStart);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取成功",jsonBack.getString("message"));

		jsonarr = jsonBack.getJSONArray("contents");

		assertEquals(2,jsonarr.size());

		jsonObject.put("type","getRecordByCourse");
		jsonObject.remove("courseId");
		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("courseId",12);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取成功",jsonBack.getString("message"));

		jsonObject.put("type","logout");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("已登出",jsonBack.getString("message"));

		jsonObject = new JSONObject();

		jsonObject.put("type","login");
		jsonObject.put("userId",1024);
		jsonObject.put("token","1001");
		jsonObject.put("password","admin");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));

		token = jsonBack.getString("newToken");
		
		jsonObject.put("token",token);

		jsonObject.put("type","getRecordByStudent");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("studentId",20001);
		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("错误\n不是学生",jsonBack.getString("message"));		

		jsonObject.put("studentId",10001);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取成功",jsonBack.getString("message"));
		
		jsonObject.put("type","getAllCourses");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取成功",jsonBack.getString("message"));

		arr = jsonBack.getJSONArray("contents");

		assertEquals(1,arr.size());

		jsonObject.put("type","getAllUsers");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("获取成功",jsonBack.getString("message"));

		arr = jsonBack.getJSONArray("contents");

		assertEquals(6,arr.size());

		jsonObject.put("type","deleteStudent2Course");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("studentId",10001);
		jsonObject.put("courseId",13);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("不存在学生或课程",jsonBack.getString("message"));

		jsonObject.put("courseId",12);
		jsonObject.put("studentId",10);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("不存在学生或课程",jsonBack.getString("message"));

		jsonObject.put("studentId",10001);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功移除",jsonBack.getString("message"));

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("已经移除过了",jsonBack.getString("message"));

		jsonObject.put("type","deleteUser");
		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("deleteId",1023);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("不存在此用户",jsonBack.getString("message"));

		jsonObject.put("deleteId",1024);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("删除失败...\n您无权限删除用户",jsonBack.getString("message"));

		jsonObject.put("deleteId",10001);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("删除用户成功",jsonBack.getString("message"));

		jsonObject.put("deleteId",20002);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("删除用户成功",jsonBack.getString("message"));

		jsonObject.put("deleteId",20001);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("此用户是老师且有未教完的课，不能删除",jsonBack.getString("message"));

		jsonObject.put("deleteId",10002);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("删除用户成功",jsonBack.getString("message"));

		jsonObject.put("deleteId",10004);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("删除用户成功",jsonBack.getString("message"));

		jsonObject.put("type","deleteCourse");
		jsonObject.remove("courseId");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("JSON参数错误",jsonBack.getString("message"));

		jsonObject.put("courseId",12);
		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("删除课程成功",jsonBack.getString("message"));

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("不存在此课程",jsonBack.getString("message"));

		jsonObject.put("type","deleteUser");
		jsonObject.put("deleteId",20001);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("删除用户成功",jsonBack.getString("message"));

		jsonObject.put("deleteId",10003);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("删除用户成功",jsonBack.getString("message"));

		jsonObject.put("type","logout");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("已登出",jsonBack.getString("message"));

		jsonObject = new JSONObject();
		jsonObject.put("type","login");
		jsonObject.put("userId",1);
		jsonObject.put("token","1001");
		jsonObject.put("username","root");
		jsonObject.put("password","root");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("成功",jsonBack.getString("message"));

		token = jsonBack.getString("newToken");

		jsonObject.put("type","deleteUser");
		jsonObject.put("token",token);

		jsonObject.put("deleteId",1024);

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("删除用户成功",jsonBack.getString("message"));

		jsonObject.put("type","logout");

		response = new MockHttpServletResponse();

		userServlet.doGet(request, response);

		jsonBack = JSONObject.parseObject(new String(response.getContentAsByteArray(),"utf-8"));

		assertEquals("已登出",jsonBack.getString("message"));
	}
	
}
