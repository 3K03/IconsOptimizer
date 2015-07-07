package net.threekzerothree.icons.optimizer;

public class Drawable { 
	
	String type;
	String category;
	String drawable;
	
	public void setType(String type) {
		type = type == "" ? "APP" : type;
		this.type = type;
	}
	
	public void setCategory(String category) {
		category = category == "" ? "Application" : category;
		this.category = category;
	}
	
	public void setDrawable(String drawable) {
		this.drawable = drawable;
	}
	
	public String getType() {
		return type;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getDrawable() {
		return drawable;
	}
}
