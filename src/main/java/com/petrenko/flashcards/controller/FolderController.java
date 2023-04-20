package com.petrenko.flashcards.controller;

import com.petrenko.flashcards.dto.*;
import com.petrenko.flashcards.model.Folder;
import com.petrenko.flashcards.service.FolderService;
import com.petrenko.flashcards.service.SetOfCardsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping()
public class FolderController {
    private final static Logger LOGGER = LoggerFactory.getLogger(FolderController.class);
    private final FolderService folderService;
    private final SetOfCardsService setOfCardsService;

    @Autowired
    public FolderController(final FolderService folderService,
                            final SetOfCardsService setOfCardsService) {
        this.folderService = folderService;
        this.setOfCardsService = setOfCardsService;
    }

    @GetMapping("/folder")
    public ModelAndView getAllFolders(Principal principal,
                                      ModelAndView modelAndView) {
        LOGGER.info("invoked");

        final String userId = principal.getName();
        LOGGER.info("userId {}", userId);

        final List<FolderIdNameDto> foldersIdNameDto = folderService.getFoldersIdNameDtoByPersonId(userId);
        LOGGER.info("List<FolderIdNameDto> ByPersonId {}", foldersIdNameDto);
        modelAndView.addObject("foldersIdNameDto", foldersIdNameDto);

        modelAndView.setViewName("foldersAll");
        LOGGER.info("before show foldersAll.html");

        return modelAndView;
    }

    @GetMapping("/folder/create")
    public ModelAndView getFolderForm(ModelAndView modelAndView) {
        LOGGER.info("invoked");

        final FolderCreateDto folderCreateDto = new FolderCreateDto();
        LOGGER.info("new folderCreateDto {}", folderCreateDto);
        modelAndView.addObject("folderCreateDto", folderCreateDto);

        modelAndView.setViewName("folderCreate");
        LOGGER.info("before show folderCreate.html");
        return modelAndView;
    }

    @PostMapping("/folder/create")
    public ModelAndView saveNewFolder(@ModelAttribute FolderCreateDto folderCreateDto,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      ModelAndView modelAndView) {
        LOGGER.info("folderCreateDto from form {}", folderCreateDto);
        if (bindingResult.hasErrors()) {
            LOGGER.info("return with input error {}", folderCreateDto);
            modelAndView.addObject("folderCreateDto", folderCreateDto);
            modelAndView.setViewName("folderCreate");
            return modelAndView;
        }

        final String userId = principal.getName();
        LOGGER.info("userId {}", userId);

        final Folder savedFolder = folderService.saveFolderCreateDtoToFolder(userId, folderCreateDto);
        LOGGER.info("savedFolder {}", savedFolder);

        modelAndView.setViewName("redirect:/folder");
        LOGGER.info("before redirect:/folder");

        return modelAndView;
    }

    @GetMapping("/folder/{id}")
    public ModelAndView getFolderById(@PathVariable("id") String id,
                                      Principal principal,
                                      ModelAndView modelAndView) {
        LOGGER.info("folder id from link: {}", id);

        final String userId = principal.getName();
        LOGGER.info("userId {}", userId);

        final FolderByIdDto folderByIdDto = folderService.getFolderByIdDto(userId, id);
        LOGGER.info("folderByIdDto: {}", folderByIdDto);
        modelAndView.addObject("folderByIdDto", folderByIdDto);

        final List<SetIdNameDto> setsIdNameDto = setOfCardsService.getByFolderId(id);
        LOGGER.info("List<setsIdNameDto> getByFolderId: {}", setsIdNameDto);
        modelAndView.addObject("setsIdNameDto", setsIdNameDto);

        modelAndView.setViewName("folderById");
        LOGGER.info("before show folderById.html");
        return modelAndView;
    }

    @GetMapping("/folder/{id}/edit")
    public ModelAndView getFolderEditForm(@PathVariable("id") String id,
                                          ModelAndView modelAndView) {
        LOGGER.info("folder id from link: {}", id);

        final FolderIdNameDescriptionDto folderIdNameDescriptionDto = folderService.getFolderIdNameDescriptionDto(id);
        LOGGER.info("folderIdNameDescriptionDto: {}", folderIdNameDescriptionDto);
        modelAndView.addObject("folderIdNameDescriptionDto", folderIdNameDescriptionDto);

        modelAndView.setViewName("folderEdit");
        LOGGER.info("before show folderEdit.html");
        return modelAndView;
    }

    @PutMapping("/folder/{id}/edit")
    public ModelAndView editFolder(@ModelAttribute FolderIdNameDescriptionDto folderIdNameDescriptionDto,
                                Principal principal,
                                BindingResult bindingResult,
                                ModelAndView modelAndView) {
        LOGGER.info("folderIdNameDescriptionDto from form: {}", folderIdNameDescriptionDto);
        if (bindingResult.hasErrors()) {
            LOGGER.info("return with input error {}", folderIdNameDescriptionDto);
            modelAndView.addObject("folderIdNameDescriptionDto", folderIdNameDescriptionDto);
            modelAndView.setViewName("folderEdit");
            return modelAndView;
        }

        final String userId = principal.getName();
        LOGGER.info("userId {}", userId);

        final Folder updatedFolder = folderService.updateFolderByFolderIdNameDescriptionDto(userId, folderIdNameDescriptionDto);
        LOGGER.info("updatedFolder {}", updatedFolder);

        String red = "redirect:/folder/" + folderIdNameDescriptionDto.getId();
        modelAndView.setViewName(red);
        LOGGER.info("before {}", red);
        return modelAndView;
    }

    @GetMapping("/folder/{id}/delete")
    public ModelAndView getFolderDeleteForm(@PathVariable("id") String id,
                                            ModelAndView modelAndView) {

        LOGGER.info("folder id from link: " + id);

        final FolderIdNameDescriptionDto folderIdNameDescriptionDto = folderService.getFolderIdNameDescriptionDto(id);
        LOGGER.info("folderIdNameDescriptionDto: {}", folderIdNameDescriptionDto);
        modelAndView.addObject("folderIdNameDescriptionDto", folderIdNameDescriptionDto);

        final List<SetIdNameDto> setsIdNameDto = setOfCardsService.getByFolderId(id);
        LOGGER.info("List<setsIdNameDto> getByFolderId: {}", setsIdNameDto);
        modelAndView.addObject("setsIdNameDto", setsIdNameDto);

        modelAndView.setViewName("folderDeleteById");
        LOGGER.info("before show folderDeleteById.html");
        return modelAndView;
    }

    @DeleteMapping("/folder/{id}/delete")
    public ModelAndView deleteFolder(@PathVariable("id") String id,
                                     ModelAndView modelAndView) {
        LOGGER.info("folder id from link: " + id);

        folderService.deleteAllByFolderId(id);
        LOGGER.info("folder is deleted");

        modelAndView.setViewName("redirect:/folder");
        LOGGER.info("before redirect:/folder");
        return modelAndView;

    }

}
