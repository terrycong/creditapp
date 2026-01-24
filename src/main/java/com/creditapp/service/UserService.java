package com.creditapp.service;

import com.creditapp.dto.*;
import com.creditapp.entity.Child;
import com.creditapp.entity.User;

import java.util.List;

public interface UserService {
    Child createChild(Long parentId, String username, String password);
    void adjustChildPoints(Long childId, Integer points);
    ChildDTO getChildById(Long id);
    List<ChildDTO> getChildrenByParentId(Long parentId);
    java.util.Optional<User> findByUsername(String username);
    User findById(Long id);
    
    // New methods for child management
    ChildDTO updateChild(Long childId, UpdateChildRequest request);
    void deleteChild(Long childId);
    ChildDetailsDTO getChildDetails(Long childId);
}
