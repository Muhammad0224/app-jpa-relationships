package uz.pdp.appjparelationships.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import uz.pdp.appjparelationships.entity.Address;
import uz.pdp.appjparelationships.entity.Group;
import uz.pdp.appjparelationships.entity.Student;
import uz.pdp.appjparelationships.entity.Subject;
import uz.pdp.appjparelationships.payload.StudentDto;
import uz.pdp.appjparelationships.repository.AddressRepository;
import uz.pdp.appjparelationships.repository.GroupRepository;
import uz.pdp.appjparelationships.repository.StudentRepository;
import uz.pdp.appjparelationships.repository.SubjectRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    StudentRepository studentRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    AddressRepository addressRepository;

    //1. VAZIRLIK
    @GetMapping("/forMinistry")
    public Page<Student> getStudentListForMinistry(@RequestParam int page) {
        //1-1=0     2-1=1    3-1=2    4-1=3
        //select * from student limit 10 offset (0*10)
        //select * from student limit 10 offset (1*10)
        //select * from student limit 10 offset (2*10)
        //select * from student limit 10 offset (3*10)
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAll(pageable);
    }

    //2. UNIVERSITY
    @GetMapping("/forUniversity/{universityId}")
    public Page<Student> getStudentListForUniversity(@PathVariable Integer universityId,
                                                     @RequestParam int page) {
        //1-1=0     2-1=1    3-1=2    4-1=3
        //select * from student limit 10 offset (0*10)
        //select * from student limit 10 offset (1*10)
        //select * from student limit 10 offset (2*10)
        //select * from student limit 10 offset (3*10)
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAllByGroup_Faculty_UniversityId(universityId, pageable);
    }

    //3. FACULTY DEKANAT
    @GetMapping("/forFaculty/{facultyId}")
    public Page<Student> getStudentListForFaculty(@PathVariable Integer facultyId,
                                                  @RequestParam int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAllByGroup_FacultyId(facultyId, pageable);
    }

    //4. GROUP OWNER
    @GetMapping("/forGroup/{groupId}")
    public Page<Student> getStudentListForOwner(@PathVariable Integer groupId,
                                                @RequestParam int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAllByGroupId(groupId, pageable);
    }

    @PostMapping
    public String add(@RequestBody StudentDto dto) {
        Integer groupId = dto.getGroupId();
        if (!groupRepository.existsById(groupId))
            return "Group not found";

        List<Integer> subjectIds = dto.getSubjectIds();
        for (Integer subjectId : subjectIds) {
            if (!subjectRepository.existsById(subjectId))
                return "Subject not found";
        }

        String city = dto.getCity();
        String district = dto.getDistrict();
        String street = dto.getStreet();
        String firstName = dto.getFirstName();
        String lastName = dto.getLastName();
        Group group = groupRepository.getOne(dto.getGroupId());
        List<Subject> subjectList = new ArrayList<>();

        Address address = addressRepository.save(Address.builder().city(city).district(district).street(street).build());

        for (Integer subjectId : subjectIds) {
            subjectList.add(subjectRepository.getOne(subjectId));
        }

        studentRepository.save(Student.builder().address(address).group(group).firstName(firstName).lastName(lastName).subjects(subjectList).build());
        return "Student added";
    }

    @PutMapping("/{id}")
    public String edit(@PathVariable Integer id, @RequestBody StudentDto dto) {
        if (!studentRepository.existsById(id))
            return "Student not found";

        Student editingStudent = studentRepository.getOne(id);

        Integer addressId = editingStudent.getAddress().getId();

        if (!addressRepository.existsById(addressId))
            return "Address not found";

        Integer groupId = dto.getGroupId();
        if (!groupRepository.existsById(groupId))
            return "Group not found";

        List<Integer> subjectIds = dto.getSubjectIds();
        for (Integer subjectId : subjectIds) {
            if (!subjectRepository.existsById(subjectId))
                return "Subject not found";
        }

        Address address = addressRepository.getOne(addressId);
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setStreet(dto.getStreet());

        addressRepository.save(address);

        String firstName = dto.getFirstName();
        String lastName = dto.getLastName();
        Group group = groupRepository.getOne(dto.getGroupId());
        List<Subject> subjectList = new ArrayList<>();
        for (Integer subjectId : subjectIds) {
            subjectList.add(subjectRepository.getOne(subjectId));
        }

        editingStudent.setAddress(address);
        editingStudent.setGroup(group);
        editingStudent.setFirstName(firstName);
        editingStudent.setSubjects(subjectList);
        editingStudent.setLastName(lastName);

        studentRepository.save(editingStudent);
        return "Student edited";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id){
        if (!studentRepository.existsById(id))
            return "Student not found";

        studentRepository.deleteById(id);
        return "Student deleted";
    }

}
