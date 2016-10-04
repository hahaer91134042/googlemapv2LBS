package tcnr06.com;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.content.ContentResolver;
import android.content.ContentValues;

import android.database.Cursor;
import android.net.Uri;

import android.util.Log;


public class SQLiteWriter {
    static String TAG="tcnr6==>";
	
	public static String getSystemTime() {
		SimpleDateFormat Dateformatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		 Date curDate = new Date(System.currentTimeMillis()); //  獲取當前時間
		 String sysTime = Dateformatter.format(curDate);
		 
		return sysTime;
	}
	public static long parseSaveDate(String getDate) {
	    long saveDate=0;
		SimpleDateFormat Dateformatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			saveDate = Dateformatter.parse(getDate).getTime();
			Log.d(TAG, "parse_getSystime=>" + getDate);
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		return saveDate;
	}

	public static void wirteToSQLite(ContentResolver mContRes, Uri uri,
			String[] COLUMN, ContentValues newRow) {
		
		Cursor c = mContRes.query(uri, COLUMN, null, null, null);
		c.moveToFirst();// 一定要寫，不然會出錯	
		// --------insert SQLite------------------
		mContRes.insert(uri, newRow);// insert
		//--------------------------
		c.close();
		
	}

	public static void sqlDelete(ContentResolver mContRes, Uri uri, String[] COLUMN) {
		Cursor c = mContRes.query(uri, COLUMN, null, null, null);
		c.moveToFirst();// 一定要寫，不然會出錯	
		//-----------delete---
		mContRes.delete(uri, null, null); // 刪除所有資料
		//------------------
		c.close();
		
	}

	public static void sqlUpdate(ContentResolver mContRes, Uri uri, String[] COLUMN,
			ContentValues newRow, String whereClause) {
		Cursor c = mContRes.query(uri, COLUMN, null, null, null);
		c.moveToFirst();// 一定要寫，不然會出錯
		//--------update--------
		mContRes.update(uri, newRow, whereClause, null);
		//-----------------------
		c.close();
	}

	
	
	public static String[][] getSQLiteData(ContentResolver mContRes, Uri uri,
			String[] COLUMN) {
		
		Cursor c = mContRes.query(uri, COLUMN, null, null, null);
		c.moveToFirst();// 一定要寫，不然會出錯
		
		int columNum=c.getColumnCount();
		int count=c.getCount();
	
		String[][] getValue = new String[count][columNum] ;
		Log.d(TAG, "getCount=>"+count+"getCoumn=>"+columNum);
		
		if(count>0&&columNum>0){
			for(int i=0;i<count;i++){
				c.moveToPosition(i);
				for(int j=0;j<columNum;j++){
					getValue[i][j]=c.getString(j);				
					
				}				
			}	
			Log.d(TAG, "getValue=>"+getValue);
			c.close();
			return getValue;
		}else {
			c.close();
			return null;
		}
		

				
	}


   

}
