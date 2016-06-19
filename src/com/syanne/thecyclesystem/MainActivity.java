package com.syanne.thecyclesystem;

import Processor.*;
import java.text.SimpleDateFormat;
import java.util.*;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * 
 * @author Anna
 * деятельность
 */
public class MainActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, OnClickListener {	 
    public ArrayList<Note> notes;
    private DataAdapter dataAdapter;    
   	private JsonFileProcessor jfp;
   	private MyGestureListener myGestureListener;
    
    ListView listView1;
    Calendar calendar;
    SimpleDateFormat df;
    AlertDialog alert;
    View promptView;
   	int currentPosition;
    
   	/**
   	 * создание Activity
   	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar sab = this.getActionBar();
        sab.hide();
        
        //инициализация переменных
        df = new SimpleDateFormat(getString(R.string.date_format));
        alert = null;
        jfp = new JsonFileProcessor(this);        
        
        //подготовка данных для вывода
        calendar = Calendar.getInstance(Locale.getDefault());
        prepareItems();
        
        dataAdapter = new DataAdapter(this, R.id.listView1, notes);
        
        listView1 = (ListView)findViewById(R.id.listView1);
        listView1.setAdapter(dataAdapter);
        listView1.setOnItemClickListener(this);        
        
        myGestureListener = new MyGestureListener(this);
        
        String formattedDate = df.format(calendar.getTime());
        ((TextView)this.findViewById(R.id.textView1)).setText(formattedDate);
        
        //слушатели
        ((ListView)findViewById(R.id.listView1)).setOnTouchListener(myGestureListener);       
        ((ListView)findViewById(R.id.listView1)).setOnItemLongClickListener(this);
        ((ImageButton)this.findViewById(R.id.add_button)).setOnClickListener(this); 
        ((ImageButton)this.findViewById(R.id.nav_back_button)).setOnClickListener(this);
        ((ImageButton)this.findViewById(R.id.nav_next_button)).setOnClickListener(this); 
        ((ImageButton)this.findViewById(R.id.nav_next_button)).setVisibility(View.INVISIBLE); 
    }
    
    /**
     * подготовка списка записей
     * @return список записей
     */
    private ArrayList<Note> prepareItems(){
        //preparing    	
    	notes =  jfp.loadData(calendar, df);
    	
        if(notes.size() >= 2){
        	this.makeSort();
        }
        
        return notes;
    }
    
    /**
     * сортировка записей
     */
    protected void makeSort(){
    	//по приоритету
        Collections.sort(notes, new Comparator<Note>() {
        	@Override
            public int compare(Note note1, Note note2)
            {
                return  note1.priority.compareTo(note2.priority);
            }
        });    
        
        //по выполнению
        Collections.sort(notes, new Comparator<Note>() {
			@Override
			public int compare(Note note1, Note note2) {
				int val1 = (note1.isDone == false)?  0: 1;
				int val2 = (note2.isDone == false)?  0: 1;
				return Integer.compare(val1, val2);
			}
        }); 
    }
    
    /**
     * обработка нажатия
     */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {		
		if(!notes.get(position).isDone)
		{
			currentPosition = position;
			if(alert == null)
			{
				makeDial();	
				setEditors();
			}
			else 
			{
				setEditors();
			}
			alert.show();
		}
	}
	
	/**
	 * обновление полей в окне редактирования
	 */
	private void setEditors(){
		((EditText) promptView.findViewById(R.id.text_dial)).setText(notes.get(currentPosition).text);		
		((EditText) promptView.findViewById(R.id.repeat_deal)).setText(Integer.toString(notes.get(currentPosition).repeat));		
		double d = Double.parseDouble(notes.get(currentPosition).time);
		((EditText) promptView.findViewById(R.id.time_deal)).setText(Double.toString(d));
		
		String pri = notes.get(currentPosition).priority;
		int selected = (pri == "A")? 0 : (pri == "B")? 1 : 2;
		((Spinner) this.promptView.findViewById(R.id.priorities_list_dial)).setSelection(selected);
	}
	
	/**
	 * создание диалога
	 */
	private void makeDial(){
		LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
		promptView = layoutInflater.inflate(R.layout.dialog_template, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
		alertDialogBuilder.setView(promptView);
		
		// setup a dialog window
		alertDialogBuilder.setCancelable(false)
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) 
					{
						addChangeNote();
					}
				});				

		// create an alert dialog
		alert = alertDialogBuilder.create();
	}
	
	/**
	 * обработка добавления/редактирования записи
	 */
	private void addChangeNote(){		
		//получаем данные из окна ввода
		Note note = (currentPosition == -1)? new Note(): notes.get(currentPosition);
		
		String text = ((EditText) promptView.findViewById(R.id.text_dial)).getText().toString();	
		int repeat = Integer.valueOf(((EditText) promptView.findViewById(R.id.repeat_deal)).getText().toString());	
		String time = ((EditText) promptView.findViewById(R.id.time_deal)).getText().toString();
		String priority = ((Spinner)promptView.findViewById(R.id.priorities_list_dial)).getSelectedItem().toString();

		//задаем значения
		if(text.compareTo("") != 0)
		{
			Note oldNote = note.cloneNote();
			
			note.text = text;
			note.priority = priority;
			note.time = time;
			note.repeat = repeat;
			
			if(currentPosition == -1){
				note.dateStart = df.format(calendar.getTime());
				note.dateFinish = df.format(new Date(0,0,0));
				note.isDone = false;
				notes.add(note); 
				try {
					jfp.createNote(note, true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					String str = e.getMessage();
				}
			}
			else{
				jfp.changeNote(note, oldNote, true);
			}
	        this.makeSort();
			dataAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * удаление записи
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) 
	{	
		//удаление элемента
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		builder.setMessage(R.string.dialog_message)
		       .setTitle(R.string.dialog_title);
		
		//определение позиции элемента в списке
		this.currentPosition = position;
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) 
	           {
	   				jfp.removeNote(notes.get(currentPosition), true);
	               	notes.remove(currentPosition);
	   				dataAdapter.notifyDataSetChanged();
	   				currentPosition = -1;
	           }
	       });
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               // User cancelled the dialog
	           }
	       });
		
		//вывод диалога
		AlertDialog dialog = builder.create();
		dialog.show();
		return true;
	}
	
	/**
	 * обработка нажатия а кнопку
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id){
		case R.id.nav_next_button:
			 this.changeDate(1);
			 break;
		case R.id.nav_back_button: 
			this.changeDate(-1);
			break;
		case R.id.add_button: 
			currentPosition = -1;
			
			if(alert == null)
			{
				makeDial();	
			}

			((EditText) promptView.findViewById(R.id.text_dial)).setText("");		
			((EditText) promptView.findViewById(R.id.repeat_deal)).setText(Integer.toString(0));		
			double d = Double.parseDouble("0.00");
			((EditText) promptView.findViewById(R.id.time_deal)).setText(Double.toString(d));
			((Spinner) this.promptView.findViewById(R.id.priorities_list_dial)).setSelection(0);
			alert.show();
				
			break;
		default: break;
		}			
	}
	
	/**
	 * изменение даты
	 * @param amountOfDays количество дней
	 */
	private void changeDate(int amountOfDays){
		
		Calendar cale = calendar;
		cale.add(Calendar.DATE, amountOfDays);

		Date first = Calendar.getInstance(Locale.getDefault()).getTime();
		Boolean isAfter = cale.after(first);
		
		if(df.format(first).equals(df.format(cale.getTime()))){
	        ((ImageButton)this.findViewById(R.id.nav_next_button)).setVisibility(View.INVISIBLE);
		}
		else ((ImageButton)this.findViewById(R.id.nav_next_button)).setVisibility(View.VISIBLE);
		
		if(isAfter == false){
			String formattedDate = df.format(calendar.getTime());
	        ((TextView)this.findViewById(R.id.textView1)).setText(formattedDate);
	        
			notes = jfp.loadData(calendar, df);
			
		     this.makeSort();
		     dataAdapter = new DataAdapter(this, R.id.listView1, notes);	        
		     listView1.setAdapter(dataAdapter);
		     listView1.setOnItemClickListener(this);dataAdapter.notifyDataSetChanged(); 
		} 
		
		
	}
	
	/**
	 * 
	 * @author Anna
	 * Обработка движения пальца (отметка выполнения записи)
	 */
	class MyGestureListener extends SimpleOnGestureListener implements OnTouchListener
	{
	    Context context;
	    GestureDetector gDetector;

	    public MyGestureListener()
	    {
	        super();
	    }

	    public MyGestureListener(Context context) {
	        this(context, null);
	    }

	    public MyGestureListener(Context context, GestureDetector gDetector) {

	        if(gDetector == null)
	            gDetector = new GestureDetector(context, this);

	        this.context = context;
	        this.gDetector = gDetector;
	    }

	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
	    {
	    	//установка изменения элемента
	    	int id = listView1.pointToPosition((int) e1.getX(), (int) e1.getY());
	    	notes.get(id).isDone = true;
	    	notes.get(id).dateFinish = df.format(calendar.getTime());
	    	jfp.changeNote(notes.get(id), notes.get(id), true);
			dataAdapter.notifyDataSetChanged(); 
			makeSort();
	        return super.onFling(e1, e2, velocityX, velocityY);
	    }

	    @Override
	    public boolean onSingleTapConfirmed(MotionEvent e) {

	        return super.onSingleTapConfirmed(e);
	    }

	    public boolean onTouch(View v, MotionEvent event) {

	        // Within the MyGestureListener class you can now manage the event.getAction() codes.

	        // Note that we are now calling the gesture Detectors onTouchEvent. And given we've set this class as the GestureDetectors listener 
	        // the onFling, onSingleTap etc methods will be executed.
	        return gDetector.onTouchEvent(event);
	    }

	    public GestureDetector getDetector()
	    {
	        return gDetector;
	    }       
	}
}



