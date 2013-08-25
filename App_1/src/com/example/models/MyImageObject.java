package com.example.models;

public class MyImageObject {
	private Long id;
	private String imagePath;
	private String audioPath;
	private String description;
	private Long category_fk;
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}
	
	public MyImageObject(){
		
	}
	
	public MyImageObject(String imagePath){
		this.imagePath= imagePath;
	}
	

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getCategory_fk() {
		return category_fk;
	}

	public void setCategory_fk(Long category_fk) {
		this.category_fk = category_fk;
	}
	
	@Override
	public String toString(){
		return id+" "+imagePath+" "+description;
		
	}


	public String getAudioPath() {
		return audioPath;
	}


	public void setAudioPath(String audioPath) {
		this.audioPath = audioPath;
	}

}
