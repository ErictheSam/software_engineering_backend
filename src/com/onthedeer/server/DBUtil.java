package com.onthedeer.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.io.*;

import com.alibaba.fastjson.JSONObject; 

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author eric
 *
 */

public class DBUtil{

	private DBUtil(){
		throw new IllegalStateException("Utility class");
	}

    private static Connection conn;
    
    private static String url;
    private static String username;
    private static String password;
    private static JSONObject jsonObject;

    private static final Logger LOGGER = LogManager.getLogger(DBUtil.class);
    
    static{
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch(Exception e){
            LOGGER.error("Failed to load jdbc driver");
        }
        String jsonRst = LoadJson.loadJsonFile("resource_test.json","webapps/onthedeer/WEB-INF/resource.json");
        jsonObject = JSONObject.parseObject(jsonRst);
        url = jsonObject.getString("url");
        username = jsonObject.getString("username");
        password = jsonObject.getString("password");
    }
    
    public static Connection getConn(){
        try{
            conn = DriverManager.getConnection(url, username, password);
        }
        catch (Exception e) {
            LOGGER.error("Failed to get connection:",e);
        }
        return conn;
    }
    
    public static void closeConn(){
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.error("Failed to close connection:",e);
            }
 
        }
    }
 
}
