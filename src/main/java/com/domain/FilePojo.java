package com.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/*
 * 	文件的实体类,方便查找或遍历文件
 * 	layer、parent=0表示该文件当前为根目录
 */
@Data
public class FilePojo implements Serializable{
	
	private Integer id;
	private Integer parentId;
	private String type;
	private String name;
	private Date createTime;
	private String virtualPath;
	private String typeIco;
	private String size;
	
	public FilePojo() {
		super();
	}
	public FilePojo(Integer id, Integer parentId, String type, String name, Date createTime,
			String virtualPath, String typeIco, String size) {
		super();
		this.id = id;
		this.parentId = parentId;
		this.type = type;
		this.name = name;
		this.createTime = createTime;
		this.virtualPath = virtualPath;
		this.typeIco = typeIco;
		this.size = size;
	}
	
}
