package com.onthedeer.server;
import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoadJson{
    
    private LoadJson(){throw new IllegalStateException("Utility class");}
    private static final Logger LOGGER = LogManager.getLogger(LoadJson.class);
    
    protected static String loadJsonFile(String testStr, String deployStr) {
        String jsonStr = testStr;
        File testFile = new File(jsonStr);
        if(!testFile.exists()){jsonStr = deployStr;}
        FileReader fileReader = null;
        Reader reader = null;
        try {
            File jsonFile = new File(jsonStr);
            fileReader = new FileReader(jsonFile);
    
            reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
                }           
            jsonStr = sb.toString();
                
            return jsonStr;           
            }catch (Exception e) {
                LOGGER.error("Failed to load resources:",e);
                return "";
            }finally{
                try{
                    if(fileReader != null)
                        fileReader.close();
                }catch(Exception e){LOGGER.error("Failed to load reader:",e);}
                try{
                    if(reader != null)
                        reader.close();
                }catch(Exception e){LOGGER.error("Failed to load reader:",e);}
            }
    }
}