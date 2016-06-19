package Processor;

/**
 * 
 * @author Anna
 * Хранение данных о записи
 */
public class Note {
	public String text;
	public String priority;
	public String time;
	public int repeat;
	public String dateStart;
	public String dateFinish;
	public Boolean isDone;
	
	public Note()
	{
		super();
	}
	
	/**
	 * Клонирование объекта
	 * @return клонированная запись
	 */
	public Note cloneNote() {		
		
		Note newNote = new Note();
		
		newNote.dateFinish = this.dateFinish;
		newNote.priority = this.priority;
		newNote.time = this.time;
		newNote.text = this.text;
		newNote.dateStart = this.dateStart;
		newNote.repeat = this.repeat;
		newNote.isDone = this.isDone;
		
		return newNote;
	}
}
