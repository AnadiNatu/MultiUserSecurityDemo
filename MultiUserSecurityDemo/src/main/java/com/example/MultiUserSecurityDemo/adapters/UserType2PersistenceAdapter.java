package com.example.MultiUserSecurityDemo.adapters;


import com.example.MultiUserSecurityDemo.port.UserType2Port;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserType2PersistenceAdapter implements UserType2Port {

    @Autowired
    private UserType1Repository userRepository;

    @Autowired
    private UserType1Mapper userMapper;

    @Override
    public Optional<UserType1> findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .map(userMapper::toEntity);
    }

    @Override
    public UserType1 save(UserType1 user) {
        UserType1 entity = userMapper.toEntity(user);
        UserType1 savedEntity = userRepository.save(entity);
        return userMapper.toEntity(savedEntity);
    }

    @Override
    public Optional<UserType1> findById(Long id) {
        return userRepository
                .findById(id)
                .map(userMapper::toEntity);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
