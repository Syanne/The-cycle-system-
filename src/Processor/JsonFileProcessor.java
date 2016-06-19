package Processor;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import org.json.*;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.KITKAT)
/**
 * 
 * @author Anna
 * Обработка файла
 */
public class JsonFileProcessor {	
	Context context;
	File file;
	JSONArray jarr;
	
	/**
	 * 
	 * @param context вызвавшая Activity
	 */
	public JsonFileProcessor(Context context){
		this.context = context;

		//определяем файл, в котором хранятся данные
		String filePath = context.getFilesDir().getPath().toString() + "/thecyclesys.json";
		file = new File(filePath);	
		//задаем массив объектов
		this.setJSONArray();
	}
	
	/**
	 * получение или создание массива объектов JSON
	 */
	private void setJSONArray(){		
		String result = "";
		//чтение массива из файла
	    try {
	        BufferedReader br = new BufferedReader(new FileReader(file));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            line = br.readLine();
	        }
	        result = sb.toString();
	        jarr = new JSONArray(result);
	    } 
	    //в случае ошибки создаем новый массив
	    catch(Exception e) {
	        jarr =  new JSONArray();
	    }	    
	}
	
	/**
	 * 
	 * @param note запись
	 * @param saveFile флаг сохранения файла
	 */
	public void createNote(Note note, Boolean saveFile)
	{	
		try{
			//создать объект
			JSONObject obj = new JSONObject();
			obj.put("text", note.text);
			obj.put("time", note.time);
			obj.put("priority", note.priority);
			obj.put("isDone", note.isDone);
			obj.put("dateStart", note.dateStart);
			obj.put("dateFinish", note.dateFinish);
			obj.put("repeat", note.repeat);
		
			//добавить в массив и сохранить в файл
			jarr.put(obj.toString());
		    
			if(saveFile == true)
				this.saveDocument();
			   
		} catch(Exception exx){
			
		}
	   }

	/**
	 * 
	 * @param oldNote старая запись
	 * @param saveFile флаг сохранения файла
	 */
	public void removeNote(Note oldNote, Boolean saveFile) 
	{
		try{
			//ищем старую запись			
			for(int i = 0; i < jarr.length(); i++){				
				String str = jarr.getString(i);
				JSONObject jo = new JSONObject(str);
				
				//удаляем устаревшую запись
				if(jo.optString("text").compareTo(oldNote.text) == 0 && jo.optString("dateStart").compareTo(oldNote.dateStart) == 0)
				{
					jarr.remove(i);
					break;
				}
			}
			
			//сохранить файл по надобности
			if(saveFile == true)
				this.saveDocument();
		} 
		catch(Exception exx){

		}		
	}

	/**
	 * 
	 * @param newNote обновленная запись
	 * @param oldNote старая запись
	 * @param saveFile флаг сохранения файла
	 */
	public void changeNote(Note newNote, Note oldNote, Boolean saveFile) {
		try{  				
			//удаляем старую запись
			this.removeNote(oldNote, false);
			
			//создаем новую запись
			this.createNote(newNote, saveFile);			  
			
		} catch(Exception exx){
			
		}		
	}

	/**
	 * 
	 * @param calendar дата
	 * @param df формат даты
	 * @return список записей 
	 */
	public ArrayList<Note> loadData(Calendar calendar, DateFormat df)
	{
		try
		{
			//создание списка задач
			ArrayList<Note> notes = new ArrayList<Note>();
			ArrayList<Note> oldNotes = new ArrayList<Note>();
			ArrayList<Integer> indexes = new ArrayList<Integer>();

			//задаем значения выбранной даты и календарного значения
			String current = df.format(Calendar.getInstance(Locale.getDefault()).getTime());
			String selected = df.format(calendar.getTime());
			
			for(int i = 0; i < jarr.length(); i++){
				//получаем объект из массива
				String str = jarr.getString(i);
				JSONObject jo = new JSONObject(str);
				
				//считываем данные из JSONObject 
				Note note = readNote(calendar, jo);
				
				//если выбранная не совпадает с календарной,
				//проверяем дату выполнения
				if(note.dateFinish.compareTo(selected) == 0 && note.isDone == true)
						notes.add(note);					
				else if(current.compareTo(selected) == 0)
				{							
					//поиск невыполненных задач
					if(note.isDone == false)
						notes.add(note);
					
					//поиск повторяющихся задач
					else if(note.repeat > 0){
						Calendar cal = Calendar.getInstance(Locale.getDefault());
						cal.setTime(df.parse(note.dateStart));
						cal.add(Calendar.DATE, note.repeat);
						
						if (df.format(cal.getTime()).compareTo(selected) == 0)
						{
							//клонируем устаревшие данные
							oldNotes.add(note.cloneNote());							
							indexes.add(notes.size());
							//обновляем все поля
							note.isDone = false;
							note.dateStart = df.format(cal.getTime());
							note.dateFinish = df.format(new Date(0,0,0));
							notes.add(note);							
						}							
					}
				}
			}
			//сохраняем файл, если данные были изменены
			if(indexes.size() > 0){
				for(int i = 0; i < indexes.size(); i++){
					this.changeNote(notes.get(indexes.get(i)), oldNotes.get(i), false);
				}				
				this.saveDocument();
			}
			
			return notes;
		} 
		catch (Exception ex)
		{
			return new ArrayList<Note>();
		}
	}

	/**
	 * 
	 * @param calendar дата
	 * @param obj JSONObject
	 * @return запись
	 * @throws IOException исключение поиска файла
	 */
	private Note readNote(Calendar calendar, JSONObject obj) throws IOException 
	{
		Note note = new Note();
		
		note.text = obj.optString("text", "");
		note.time = obj.optString("time", "");
		note.dateStart = obj.optString("dateStart", "");
		note.dateFinish = obj.optString("dateFinish", "");
		note.priority = obj.optString("priority", "");
		note.isDone = obj.optBoolean("isDone", false);
		note.repeat = obj.optInt("repeat", 0);
	     
	    return note;
	 }
	
	/**
	 * сохранение документа
	 */
	private void saveDocument(){
		try  
		   {				   
			   FileOutputStream f = new FileOutputStream(file);
		       PrintWriter pw = new PrintWriter(f);
		       pw.println(jarr.toString());
		       pw.flush();
		       pw.close();
		       f.close();
		   }
		   catch(Exception ex){
			   
		   }
	   }	

}
