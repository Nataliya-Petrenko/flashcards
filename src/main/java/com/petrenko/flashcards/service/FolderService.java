package com.petrenko.flashcards.service;

import com.petrenko.flashcards.dto.FolderByIdDto;
import com.petrenko.flashcards.dto.FolderCreateDto;
import com.petrenko.flashcards.dto.FolderIdNameDescriptionDto;
import com.petrenko.flashcards.dto.FolderIdNameDto;
import com.petrenko.flashcards.model.Card;
import com.petrenko.flashcards.model.Folder;
import com.petrenko.flashcards.model.SetOfCards;
import com.petrenko.flashcards.repository.CardRepository;
import com.petrenko.flashcards.repository.FolderRepository;
import com.petrenko.flashcards.repository.PersonRepository;
import com.petrenko.flashcards.repository.SetOfCardsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FolderService {
    private final static Logger LOGGER = LoggerFactory.getLogger(FolderService.class);
    private final FolderRepository folderRepository;
    private final SetOfCardsRepository setOfCardsRepository;
    private final CardRepository cardRepository;

    private final PersonRepository personRepository;

    private final PersonService personService;

    @Autowired
    public FolderService(final FolderRepository folderRepository,
                         final SetOfCardsRepository setOfCardsRepository,
                         final CardRepository cardRepository,
                         PersonRepository personRepository,
                         PersonService personService) {
        this.folderRepository = folderRepository;
        this.setOfCardsRepository = setOfCardsRepository;
        this.cardRepository = cardRepository;
        this.personRepository = personRepository;
        this.personService = personService;
    }


    public Folder saveCheckName(String userId, Folder folder) { //todo if a folder with this name exist then show a massage and suggest the choice to update the old one or create a new one with another name
        LOGGER.info("invoked");
        String folderName = folder.getName();
        final Folder finalFolder = folderRepository.findByUserIdAndName(userId, folder.getName()).orElse(new Folder());
        LOGGER.info("finalFolder {}", finalFolder);

        finalFolder.setName(folderName);
        finalFolder.setTimeOfCreation(LocalDateTime.now());
        finalFolder.setPerson(personRepository.findById(userId).orElseThrow(IllegalArgumentException::new));


//        Person person = personRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
//        LOGGER.info("person {}", person);
//        person.getFolders().add(finalFolder);
//        LOGGER.info("folder added: person {}", person);
//        Person savedPerson = personRepository.save(person);
//        LOGGER.info("savedPerson {}", savedPerson);

        Folder savedFolder = folderRepository.save(finalFolder);
        LOGGER.info("savedFolder {}", savedFolder);
        return folderRepository.save(savedFolder);
    }

    public Folder save(Folder folder) { //todo if a folder with this name exist then show a massage and suggest the choice to update the old one or create a new one with another name
        LOGGER.info("{}}", folder);
        return folderRepository.save(folder);
    }

    public Folder getById(String id) {
        Folder folder = folderRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        LOGGER.info("Folder service: getById " + folder);
        return folder;
    }

    public Optional<Folder> getByName(String name) {
        return folderRepository.findByName(name);
    }


    public void deleteById(String id) {
        List<SetOfCards> sets = setOfCardsRepository.getByFolder(getById(id));
        sets.forEach(s -> {                       // todo remove repeating from setService (with break cyclical dependence)
            List<Card> cards = cardRepository.getBySetOfCards(s);
            cards.forEach(c -> cardRepository.deleteById(c.getId()));
            setOfCardsRepository.deleteById(id);
        });
        folderRepository.deleteById(id);
    }

    public List<Folder> getFoldersByPersonId(String userId) {
        LOGGER.info("userId {}", userId);
        List<Folder> folders = folderRepository.findByPersonId(userId);
        LOGGER.info("folders {}", folders);
        return folders;
    }

    // -----------------for new DTO------------

    public List<FolderIdNameDto> getFoldersIdNameDtoByPersonId(String userId) {
        LOGGER.info("invoked");
        List<FolderIdNameDto> folderIdNameDto = folderRepository.getFoldersIdNameDtoByPersonId(userId);
        LOGGER.info("folderNameIdDto {}", folderIdNameDto);
        return folderIdNameDto;
    }

    public Folder saveFolderCreateDtoToFolder(String userId, FolderCreateDto folderCreateDto) {
        LOGGER.info("invoked");

        final Folder folder = getFolderWithNameOrNew(userId, folderCreateDto.getName());
        LOGGER.info("getFolderWithNameOrNew {}", folder);

        folder.setPerson(personService.getById(userId));
        folder.setName(folderCreateDto.getName());
        folder.setDescription(folderCreateDto.getDescription());

        Folder savedFolder = folderRepository.save(folder);
        LOGGER.info("savedFolder {}", savedFolder);

        return savedFolder;
    }

    private Folder getFolderWithNameOrNew(String userId, String folderName) {
        LOGGER.info("invoked");
        final Folder folder = folderRepository.findByUserIdAndName(userId, folderName).orElse(new Folder());
        LOGGER.info("finalFolder {}", folder);
        return folder;
    }

    @Transactional
    public FolderByIdDto getFolderByIdDto(String userId, String folderId) { // todo get by single DTO
        LOGGER.info("invoked");

        FolderIdNameDescriptionDto folderIdNameDescriptionDto = folderRepository
                .getFolderIdNameDescriptionDto(folderId).orElseThrow(IllegalArgumentException::new);

        FolderByIdDto folderByIdDto = new FolderByIdDto();

        folderByIdDto.setId(folderIdNameDescriptionDto.getId());
        folderByIdDto.setName(folderIdNameDescriptionDto.getName());
        folderByIdDto.setDescription(folderIdNameDescriptionDto.getDescription());
        folderByIdDto.setPreviousOrLastFolderId(getPreviousOrLastFolderId(userId, folderId));
        folderByIdDto.setNextOrFirstFolderId(getNextOrFirstFolderId(userId, folderId));

        LOGGER.info("folderByIdDto {}", folderByIdDto);
        return folderByIdDto;
    }

    private String getPreviousOrLastFolderId(final String userId, final String folderId) {
        LOGGER.info("invoked");
        String previousOrLastFolderId = folderRepository.getPreviousId(userId, folderId)
                .orElse(folderRepository.getLastId(userId)
                        .orElseThrow(IllegalArgumentException::new));
        LOGGER.info("nextOrFirstFolderId {}", previousOrLastFolderId);
        return previousOrLastFolderId;
    }

    private String getNextOrFirstFolderId(final String userId, final String folderId) {
        LOGGER.info("invoked");
        String nextOrFirstFolderId = folderRepository.getNextId(userId, folderId)
                .orElse(folderRepository.getFirstId(userId)
                        .orElseThrow(IllegalArgumentException::new));
        LOGGER.info("nextOrFirstFolderId {}", nextOrFirstFolderId);
        return nextOrFirstFolderId;
    }

    public FolderIdNameDescriptionDto getFolderIdNameDescriptionDto(String folderId) {
        LOGGER.info("invoked");
        FolderIdNameDescriptionDto folderIdNameDescriptionDto = folderRepository
                .getFolderIdNameDescriptionDto(folderId)
                .orElseThrow(IllegalArgumentException::new);
        LOGGER.info("folderIdNameDescriptionDto {}", folderIdNameDescriptionDto);
        return folderIdNameDescriptionDto;
    }

    @Transactional
    public Folder updateFolderIdNameDescriptionDtoToFolder(String userId, FolderIdNameDescriptionDto folderIdNameDescriptionDto) {
        LOGGER.info("invoked");

        String newName = folderIdNameDescriptionDto.getName();

        Optional<String> idByUserIdAndName = folderRepository.findIdByUserIdAndName(userId, newName);
        if (idByUserIdAndName.isPresent() && !idByUserIdAndName.get().equals(folderIdNameDescriptionDto.getId())) {
            throw new IllegalArgumentException("Folder with this newName already exist: " + newName);
        }

        String newDescription = folderIdNameDescriptionDto.getDescription();

        folderRepository.update(userId, newName, newDescription);

        Folder updatedFolder = folderRepository.findById(folderIdNameDescriptionDto.getId()) // todo delete get folder after checking work
                .orElseThrow(IllegalArgumentException::new);

        LOGGER.info("updatedFolder {}", updatedFolder);
        return updatedFolder;
    }

    @Transactional
    public void deleteAllByFolderId(String folderId) {
        List<String> setsId = folderRepository.getSetsIdByFolderId(folderId);
        setsId.forEach(s -> {
            cardRepository.deleteBySetId(s);
            setOfCardsRepository.deleteById(s);
        });
        folderRepository.deleteById(folderId);
    }
}
