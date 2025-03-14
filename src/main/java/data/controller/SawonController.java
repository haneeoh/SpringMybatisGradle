package data.controller;

import java.lang.reflect.Member;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import data.dto.SawonDto;
import data.service.SawonService;
import lombok.RequiredArgsConstructor;
import naver.storage.NcpObjectStorageService;

@Controller
@RequiredArgsConstructor
public class SawonController {

    final SawonService sawonService;
    final NcpObjectStorageService storageService;

    private String imagePath="https://kr.object.ncloudstorage.com/bitcamp-bucket-119/sawon/";
    private String bucketName="bitcamp-bucket-119";

    @GetMapping("/")
    public String mainPage()
    {
        return "sawon/mainpage";
    }

    @GetMapping({"/list"})
    public String sawonList(Model model)
    {
        List<SawonDto> list=sawonService.getSelectAllSawon();
        model.addAttribute("list", list);
        model.addAttribute("totalCount", list.size());
        model.addAttribute("imagePath", imagePath);

        return "sawon/sawonlist";
    }

    @GetMapping("/form")
    public String sawonForm()
    {
        return "sawon/swonform";
    }

    @PostMapping("/insert")
    public String sawonInsert(@ModelAttribute SawonDto dto,
                              @RequestParam("upload") MultipartFile upload)
    {
        if(upload.getOriginalFilename().equals(""))
            dto.setPhoto(null);
        else {
            String photo=storageService.uploadFile(bucketName, "sawon", upload);
            dto.setPhoto(photo);
        }

        sawonService.insertSawon(dto);
        return "redirect:./list";
    }

    @GetMapping("/delete")
    public String sawonDelete(@RequestParam int num){
        //사진 수정시 db에 저장된 파일명을 받아서 스토리지에서 삭제
        String oldFilename=sawonService.getSawon(num).getPhoto();
        storageService.deleteFile(bucketName, "sawon", oldFilename);

        sawonService.deleteSawon(num);
        return "redirect:./list";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("num") int num,Model model)
    {
        SawonDto dto=sawonService.getSawon(num);
        model.addAttribute("dto", dto);
        model.addAttribute("imagePath", imagePath);
        return "sawon/sawondetail";
    }

    @GetMapping("/updateform")
    public String updateForm(@RequestParam int num, Model model){
        SawonDto dto=sawonService.getSawon(num);
        model.addAttribute("dto",dto);
        return "sawon/updateform";
    }

    @PostMapping("/update")
    public String sawonUpdate(@ModelAttribute SawonDto dto, @RequestParam int num,
                             @RequestParam("upload")MultipartFile upload){
        if(upload.getOriginalFilename().equals(""))
            dto.setPhoto(null);
        //사진 수정시 db에 저장된 파일명을 받아서 스토리지에서 삭제
        else{String oldFilename=sawonService.getSawon(num).getPhoto();
        storageService.deleteFile(bucketName, "sawon", oldFilename);

        //새 파일 네이버 스토리지에 업로드
        String uploadFilename=storageService.uploadFile(bucketName,"sawon",upload);
        dto.setPhoto(uploadFilename);
        }

        //db에서도 수정
        sawonService.updateSawon(dto);
        return "redirect:./detail?num="+dto.getNum();
    }
}