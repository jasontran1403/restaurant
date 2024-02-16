package com.alibou.security.service.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibou.security.dto.AddCateRequest;
import com.alibou.security.dto.UpdateCateRequest;
import com.alibou.security.entity.Category;
import com.alibou.security.repository.CategoryRepository;
import com.alibou.security.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService{
	@Autowired
	CategoryRepository cateRepo;

	@Override
	public Category saveCate(AddCateRequest request) {
		// TODO Auto-generated method stub
		Category cate = new Category();
		Optional<Category> cateExisted = cateRepo.findByCateName(request.getCateName());
		if (cateExisted.isPresent()) {
			return null;
		}
		cate.setCateName(request.getCateName());
		cate.setStatus(request.isStatus());
		return cateRepo.save(cate);
	}

	@Override
	public void editCate(int id, String name) {
		// TODO Auto-generated method stub
		Category cate = cateRepo.getById(id);
		cate.setCateName(name);
		cateRepo.save(cate);
	}

	@Override
	public Page<Category> getAllCatesPageable(Pageable pageable) {
		// TODO Auto-generated method stub
		return cateRepo.findAll(pageable);
	}

	@Override
	public Category getById(int id) {
		// TODO Auto-generated method stub
		return cateRepo.getById(id);
	}

	@Override
	public List<Category> getAllCates() {
		// TODO Auto-generated method stub
		return cateRepo.findAll();
	}

	@Override
	public void updateCate(UpdateCateRequest request) {
		// TODO Auto-generated method stub
		boolean status = request.getStatus() == 0 ? true : false ;
		Category cate = cateRepo.getById(request.getId());
		cate.setCateName(request.getCateName());
		cate.setStatus(status);
		cateRepo.save(cate);
	}

	@Override
	public void toggleCateStatus(int id) {
		// TODO Auto-generated method stub
		Category cate = cateRepo.getById(id);
		boolean status = cate.isStatus();
		cate.setStatus(!status);
		System.out.println(status);
		cateRepo.save(cate);
		
	}

}
