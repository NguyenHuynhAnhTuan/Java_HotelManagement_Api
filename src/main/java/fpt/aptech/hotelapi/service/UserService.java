/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package fpt.aptech.hotelapi.service;

import fpt.aptech.hotelapi.dto.LoginDto;
import fpt.aptech.hotelapi.dto.RoleDto;
import fpt.aptech.hotelapi.dto.UserDto;
import fpt.aptech.hotelapi.models.Role;
import fpt.aptech.hotelapi.models.Users;
import fpt.aptech.hotelapi.repository.RoleRepository;
import fpt.aptech.hotelapi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author PC
 */
@Service
public class UserService {

    private UserRepository userRepo;
    private RoleRepository roleRepo;

    @Autowired
    public UserService(UserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    private Users mapToModel(UserDto userDto) {
        Users users = new Users();
        users.setUsername(userDto.getUsername());
        users.setEmail(userDto.getEmail());
        users.setPassword(userDto.getPassword());
        users.setAddress(userDto.getAddress());
        users.setPhone(userDto.getPhone());
        users.setActive(userDto.getActive());

        Role roleInfo = roleRepo.findById(userDto.getRole_id()).orElse(null);
        users.setRole_id(roleInfo);

        return users;
    }

    private UserDto mapToDto(Users users) {
        UserDto userDto = new UserDto();
        userDto.setId(users.getId());
        userDto.setUsername(users.getUsername());
        userDto.setEmail(users.getEmail());
        userDto.setPassword(users.getPassword());
        userDto.setAddress(users.getAddress());
        userDto.setPhone(users.getPhone());
        userDto.setActive(users.getActive());

        userDto.setRole_id(users.getRole_id().getId());

        RoleDto roleDto = new RoleDto(
                users.getRole_id().getId(),
                users.getRole_id().getRoleName()
        );
        userDto.setRoleInfo(roleDto);

        return userDto;
    }

    //Login Service
    public UserDto login(LoginDto loginDto) {
        Optional<Users> optionalUser = userRepo.findByEmail(loginDto.getEmail());
        if (optionalUser.isPresent()) {
                Users checkLogin = optionalUser.get();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(loginDto.getPassword(), checkLogin.getPassword())) {
                return mapToDto(checkLogin);
            }
            
        }
        return null;
    }

    //For Admin ONLY
    public UserDto createNewUser(UserDto userDto) {
        //Kiểm tra xem email đã tồn tại trong cơ sở dữ liệu hay chưa
        if (userRepo.existsByEmail(userDto.getEmail())) {
            return null; // Trả về null nếu email đã tồn tại
        }
        userDto.setActive(true);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);

        Users newUser = mapToModel(userDto);

        Users responseUser = userRepo.save(newUser);

        return mapToDto(responseUser);
    }

    public UserDto updateUser(UserDto updatedUserDto) {
        Users updateUser = mapToModel(updatedUserDto);
        updateUser.setId(updatedUserDto.getId());
        
        Users response = userRepo.save(updateUser);
        
        return mapToDto(response);
    }

    public List<UserDto> allUser() {
        List<Users> users = userRepo.findAll();
        return users.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    public List<UserDto> allStaff() {
        return userRepo.findAll()
                .stream()
                .filter(u -> u.getRole_id().getId() == 2)
                .map(mapper -> mapToDto(mapper))
                .toList();
    }
    
    public List<UserDto> allCustomer() {
        return userRepo.findAll()
                .stream()
                .filter(u -> u.getRole_id().getId() == 3)
                .map(mapper -> mapToDto(mapper))
                .toList();
    }

    public UserDto findOne(int id) {
        Users userInfo = userRepo.findById(id).orElse(null);
        return mapToDto(userInfo);
    }
    
    public UserDto findByEmail(String email) {
        return userRepo.findAll()
                .stream()
                .filter(u -> u.getEmail().equals(email))
                .map(mapper -> mapToDto(mapper))
                .findFirst().orElse(null);
    }

    public boolean changePassword(int id, String currentPassword, String newPassword
    ) {
        Optional<Users> optionalUser = userRepo.findById(id);
        if (optionalUser.isPresent()) {
            Users user = optionalUser.get();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                String encodedNewPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encodedNewPassword);
                userRepo.save(user);
                return true;
            }
        }
        return false;

    }

    //For Staff
    
    
    //For Customer
    public UserDto registerNewCustomer(UserDto userDto) {
        // Kiểm tra xem email đã tồn tại trong cơ sở dữ liệu hay chưa
        if (userRepo.existsByEmail(userDto.getEmail())) {
            return null; // Trả về null nếu email đã tồn tại
        }
        userDto.setRole_id(3);
        userDto.setActive(true);
        

        //Mã hóa mật khẩu trước khi lưu trữ vào cơ sở dữ liệu
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);

        Users newCustomer = mapToModel(userDto);

        Users responseUser = userRepo.save(newCustomer);

        return mapToDto(responseUser);
    }
    
    public UserDto registerNewGuest(UserDto newGuestDto) {
        newGuestDto.setRole_id(3);
        
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newGuestDto.getPassword());
        newGuestDto.setPassword(encodedPassword);
        
        Users newGuest = mapToModel(newGuestDto);
        
        Users response = userRepo.save(newGuest);
        
        return mapToDto(response);
    }

    //Kiểm tra trùng email
    public boolean existsByEmail(String email) {
        return userRepo.findByEmail(email) != null;
    }
    
}
