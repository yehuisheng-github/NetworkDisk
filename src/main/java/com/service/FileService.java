package com.service;

import java.util.List;
import com.domain.FilePojo;
import com.domain.ShareFilePojo;

public interface FileService {
	public int saveFile(FilePojo filePojo,String userId);
	public List<FilePojo> querySomeFile(String filename,String userId);
	public FilePojo queryExistFile(String userId,String virtualPath);
	public List<FilePojo> queryAllFile(String userId,String parentId);
	public int deleteFile(String userId,String virtualPath);
	public FilePojo updateFile(FilePojo filePojo,String userId);
	public List<FilePojo> queryClassifyFile(String userId,String type);
	public ShareFilePojo queryShareFile(String userId,String virtualPath);
}
