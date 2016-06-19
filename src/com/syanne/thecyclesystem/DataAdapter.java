package com.syanne.thecyclesystem;

import Processor.Note;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

/**
 * 
 * @author Anna
 * адаптер данных
 */
public class DataAdapter extends ArrayAdapter<Note>{
    //int layoutResourceId;    
    ArrayList<Note> notes = null;
    Context context;
    int resource;
    LayoutInflater inflater;
    
    /**
     * 
     * @param context вызвавшая Activity
     * @param resource id объекта ListView
     * @param data список данных
     */
    public DataAdapter(Context context, int resource, ArrayList<Note> data) 
    {
    	super(context, R.layout.list_item, data);        
        this.notes = data;
        this.context = context;
        this.resource = resource;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
        
    /**
     * добавление записи
     * @param note
     */
    public void setChildren(Note note){
    	this.notes.add(note);
    }
    
    /**
     * получить вид
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	try{
        View row = convertView;
        NoteHolder holder = null;

        Note sData = notes.get(position);
        
        if(row == null)
        {
        	//if(this.getItemViewType(position) == 0)
            row = inflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        	//else 
            //row = inflater.inflate(resource, null, true);
 
            holder = new NoteHolder();
            holder.priority = (TextView)row.findViewById(R.id.priority_tv);
            holder.time = (TextView)row.findViewById(R.id.time_tv);
            holder.text = (TextView)row.findViewById(R.id.text_tv);
            holder.lay = (LinearLayout)row.findViewById(R.id.lin);
            row.setTag(holder);
        }
        else
        {
            holder = (NoteHolder)row.getTag();
        }

        holder.priority.setText(sData.priority);
        holder.time.setText(sData.time + " " +context.getString(R.string.hour));
        holder.text.setText(sData.text);
        holder.text.setTag(sData.isDone);
        if(sData.isDone == true)
        	holder.lay.setBackgroundColor(Color.GRAY);
        else holder.lay.setBackgroundColor(Color.TRANSPARENT);
        
        	//holder.priority.setText("DONE");
        return row;
    	}
    	catch(Exception ex){
    		return null;
    	}
    }
 
    /**
     * 
     * @author Anna
     * промежуточный уровень хранения полей в списке
     */
    static class NoteHolder
    {
    	TextView text;
    	TextView priority;
    	TextView time;
    	LinearLayout lay;
    }
}
    
