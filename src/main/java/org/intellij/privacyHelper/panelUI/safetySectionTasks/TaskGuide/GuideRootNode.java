package org.intellij.privacyHelper.panelUI.safetySectionTasks.TaskGuide;

import com.intellij.find.*;
import com.intellij.find.impl.FindInProjectUtil;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopeUtil;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.privacyHelper.codeInspection.instances.AnnotationInstance;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.*;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.DataPracticeGroup;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil.*;

public class GuideRootNode extends BaseNode {
    boolean isAnnotation = false;
    boolean isRequired = false;
    boolean isSource = false;

    HashMap<String, ArrayList<ThirdPartySafetySectionInfo>> libraryDataMap = new HashMap<>();

    Set<VirtualFile> myFileSet = new HashSet<>();
    private static final int USAGES_PER_READ_ACTION = 100;

    void updateMyFileSet() {
        myFileSet = new HashSet<>();
        FindModel findModel = CoconutUIUtil.getFindModel(" ");

        List<FindInProjectSearchEngine.@NotNull FindInProjectSearcher> searchers = ContainerUtil.mapNotNull(
                FindInProjectSearchEngine.EP_NAME.getExtensions(), se -> se.createSearcher(findModel, myProject));

        Condition<CharSequence> patternCondition = FindInProjectUtil.createFileMaskCondition(findModel.getFileFilter());

        Condition<VirtualFile> fileMask = file -> file != null && patternCondition.value(file.getNameSequence());

        Set<VirtualFile> filesForFastWordSearch = getFilesForFastWordSearch(fileMask, searchers);
        // filter filesForFastWordSearch to only include java files
        myFileSet.addAll(filesForFastWordSearch.stream().filter(file -> file.getFileType().getName().equals("JAVA")).collect(Collectors.toList()));

        boolean canRelyOnIndices = canRelyOnSearchers(searchers);
        final Collection<VirtualFile> otherFiles = collectFilesInScope(
                filesForFastWordSearch, canRelyOnIndices, findModel, fileMask, searchers);
        // filter otherFiles to only include java files
        myFileSet.addAll(otherFiles.stream().filter(file -> file.getFileType().getName().equals("JAVA")).collect(Collectors.toList()));
    }

    private boolean searchKeywords(String keywordPattern) {
        FindModel findModel = CoconutUIUtil.getFindModel(keywordPattern);

        for (VirtualFile virtualFile : myFileSet) {
            if (searchInFile(virtualFile, findModel)) {
                return true;
            }
        }
        return false;
    }

    private boolean canRelyOnSearchers(List<FindInProjectSearchEngine.@NotNull FindInProjectSearcher>
                                               searchers) {
        return ContainerUtil.find(searchers, s -> s.isReliable()) != null;
    }

    @NotNull
    private Set<VirtualFile> getFilesForFastWordSearch(Condition<VirtualFile> fileMask,
                                                       List<FindInProjectSearchEngine.@NotNull FindInProjectSearcher>
                                                               searchers) {
        final Set<VirtualFile> resultFiles = new CompactVirtualFileSet();

        for (FindInProjectSearchEngine.FindInProjectSearcher searcher : searchers) {
            Collection<VirtualFile> virtualFiles = searcher.searchForOccurrences();
            for (VirtualFile file : virtualFiles) {
                if (fileMask.value(file)) resultFiles.add(file);
            }
        }

        return resultFiles;
    }

    private static void iterateAll(VirtualFile @NotNull [] files, @NotNull final GlobalSearchScope searchScope, @NotNull final ContentIterator iterator) {
        final FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        final VirtualFileFilter contentFilter = file -> file.isDirectory() ||
                !fileTypeManager.isFileIgnored(file) && !file.getFileType().isBinary() && searchScope.contains(file);
        for (VirtualFile file : files) {
            if (!VfsUtilCore.iterateChildrenRecursively(file, contentFilter, iterator)) break;
        }
    }

    // must return non-binary files
    @NotNull
    private Collection<VirtualFile> collectFilesInScope(
            @NotNull final Set<VirtualFile> alreadySearched, final boolean skipIndexed, FindModel findModel,
            Condition<VirtualFile> fileMask, List<FindInProjectSearchEngine.@NotNull FindInProjectSearcher> searchers) {
        VirtualFile directory = FindInProjectUtil.getDirectory(findModel);
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
        final String moduleName = findModel.getModuleName();
        Module myModule = moduleName == null ? null :
                ReadAction.compute(() -> ModuleManager.getInstance(myProject).findModuleByName(moduleName));
        FileIndex myFileIndex = myModule == null ? projectFileIndex :
                ModuleRootManager.getInstance(myModule).getFileIndex();
        SearchScope customScope = findModel.isCustomScope() ? findModel.getCustomScope() : null;
        final GlobalSearchScope globalCustomScope = customScope == null ? null :
                GlobalSearchScopeUtil.toGlobalSearchScope(customScope, myProject);

        final Set<VirtualFile> result = new CompactVirtualFileSet();

        class EnumContentIterator implements ContentIterator {

            @Override
            public boolean processFile(@NotNull final VirtualFile virtualFile) {
                ReadAction.run(() -> {
                    ProgressManager.checkCanceled();
                    if (virtualFile.isDirectory() || !virtualFile.isValid() || !fileMask.value(virtualFile) ||
                            globalCustomScope != null && !globalCustomScope.contains(virtualFile)) {
                        return;
                    }

                    if (skipIndexed && ContainerUtil.find(searchers, p -> p.isCovered(virtualFile)) != null) {
                        return;
                    }

                    Pair.NonNull<PsiFile, VirtualFile> pair = findFile(virtualFile);
                    if (pair == null) return;
                    VirtualFile sourceVirtualFile = pair.second;

                    if (sourceVirtualFile != null && !alreadySearched.contains(sourceVirtualFile)) {
                        result.add(sourceVirtualFile);
                    }
                });
                return true;
            }
        }
        final EnumContentIterator iterator = new EnumContentIterator();

        if (customScope instanceof LocalSearchScope) {
            for (VirtualFile file : GlobalSearchScopeUtil.getLocalScopeFiles((LocalSearchScope)customScope)) {
                iterator.processFile(file);
            }
        }
        else if (customScope instanceof Iterable) {  // GlobalSearchScope can span files out of project roots e.g. FileScope / FilesScope
            //noinspection unchecked
            for (VirtualFile file : (Iterable<VirtualFile>)customScope) {
                iterator.processFile(file);
            }
        }
        else if (directory != null) {
            boolean checkExcluded = !ProjectFileIndex.SERVICE.getInstance(myProject).isExcluded(directory) && !Registry.is("find.search.in.excluded.dirs");
            VirtualFileVisitor.Option limit = VirtualFileVisitor.limit(findModel.isWithSubdirectories() ? -1 : 1);
            VfsUtilCore.visitChildrenRecursively(directory, new VirtualFileVisitor<Void>(limit) {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    if (checkExcluded && projectFileIndex.isExcluded(file)) return false;
                    iterator.processFile(file);
                    return true;
                }
            });
        }
        else {
            boolean success = myFileIndex.iterateContent(iterator);
            if (success && globalCustomScope != null && globalCustomScope.isSearchInLibraries()) {
                Pair<VirtualFile[], VirtualFile[]> libraryRoots = ReadAction.compute(() -> {
                    OrderEnumerator enumerator = (myModule == null ? OrderEnumerator.orderEntries(myProject) : OrderEnumerator.orderEntries(myModule))
                            .withoutModuleSourceEntries()
                            .withoutDepModules();
                    return Pair.create(enumerator.getSourceRoots(), enumerator.getClassesRoots());
                });

                VirtualFile[] sourceRoots = libraryRoots.getFirst();
                iterateAll(sourceRoots, globalCustomScope, iterator);

                VirtualFile[] classRoots = libraryRoots.getSecond();
                iterateAll(classRoots, globalCustomScope, iterator);
            }
        }

        for (FindModelExtension findModelExtension : FindModelExtension.EP_NAME.getExtensionList()) {
            findModelExtension.iterateAdditionalFiles(findModel, myProject, file -> {
                if (!alreadySearched.contains(file)) {
                    result.add(file);
                }
                return true;
            });
        }

        return result;
    }

    private boolean searchInFile(VirtualFile virtualFile, FindModel findModel) {
        Pair.NonNull<PsiFile, VirtualFile> pair = ReadAction.compute(() -> findFile(virtualFile));
        if (pair == null) {
            return false;
        }
        PsiFile psiFile = pair.first;
        VirtualFile sourceVirtualFile = pair.second;
        final Document document = ReadAction.compute(
                () -> sourceVirtualFile.isValid() ? FileDocumentManager.getInstance().getDocument(sourceVirtualFile)
                        : null);
        if (document == null) {
            return false;
        }
        final int[] offsetRef = {0};
        int before;
        int occurrenceCount = 0;
        do {
            before = offsetRef[0];
            occurrenceCount += processSomeOccurrencesInFile(document, findModel, psiFile, offsetRef);
            if (occurrenceCount > 0) {
                return true;
            }
        }
        while (offsetRef[0] != before);
        return occurrenceCount > 0;
    }

    private static int processSomeOccurrencesInFile(@NotNull Document document,
                                                    @NotNull FindModel findModel,
                                                    @NotNull final PsiFile psiFile,
                                                    int @NotNull [] offsetRef) {
        CharSequence text = document.getCharsSequence();
        int textLength = document.getTextLength();
        int offset = offsetRef[0];

        Project project = psiFile.getProject();

        FindManager findManager = FindManager.getInstance(project);
        int count = 0;
        while (offset < textLength) {
            FindResult result = findManager.findString(text, offset, findModel, psiFile.getVirtualFile());
            if (!result.isStringFound()) break;

            final int prevOffset = offset;
            offset = result.getEndOffset();
            if (prevOffset == offset || offset == result.getStartOffset()) {
                // for regular expr the size of the match could be zero -> could be infinite loop in finding usages!
                ++offset;
            }

            final SearchScope customScope = findModel.getCustomScope();
            if (customScope instanceof LocalSearchScope) {
                final TextRange range = new TextRange(result.getStartOffset(), result.getEndOffset());
                if (!((LocalSearchScope)customScope).containsRange(psiFile, range)) continue;
            }
            count++;

            if (count >= USAGES_PER_READ_ACTION) {
                break;
            }
        }
        offsetRef[0] = offset;
        return count;
    }

    private Pair.NonNull<PsiFile, VirtualFile> findFile(@NotNull final VirtualFile virtualFile) {
        PsiManager psiManager = PsiManager.getInstance(myProject);
        PsiFile psiFile = psiManager.findFile(virtualFile);
        if (psiFile != null) {
            PsiElement sourceFile = psiFile.getNavigationElement();
            if (sourceFile instanceof PsiFile) psiFile = (PsiFile)sourceFile;
            if (psiFile.getFileType().isBinary()) {
                psiFile = null;
            }
        }
        VirtualFile sourceVirtualFile = PsiUtilCore.getVirtualFile(psiFile);
        if (psiFile == null || psiFile.getFileType().isBinary() || sourceVirtualFile == null || sourceVirtualFile.getFileType().isBinary()) {
            return null;
        }

        return Pair.createNonNull(psiFile, sourceVirtualFile);
    }

    public GuideRootNode(Project project, Object o, AbstractTreeBuilder builder) {
        super(project, o, builder);
    }

    public void setCondition(boolean isAnnotation, boolean isSource, boolean isRequired) {
        this.isAnnotation = isAnnotation;
        this.isSource = isSource;
        this.isRequired = isRequired;
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        ArrayList<AbstractTreeNode> childrenNodes = new ArrayList<>();
        if (isAnnotation) {
            if (isRequired) {
                childrenNodes.add(new AnnotationActionNode(myProject, 0,
                        isSource ? DataPracticeGroup.DETECTED_SOURCE : DataPracticeGroup.DETECTED_SINK, myBuilder,
                        true));
                childrenNodes.add(new AnnotationActionNode(myProject, 1,
                        isSource ? DataPracticeGroup.DETECTED_SOURCE : DataPracticeGroup.DETECTED_SINK, myBuilder,
                        false));
            } else {
                updateMyFileSet();
                if (isSource) {
                    Vector<AnnotationInstance> annotationInstances;
                    annotationInstances = PrivacyPracticesHolder.getInstance(getProject())
                            .getRequiredAnnotationsByType(CoconutAnnotationType.DataAccess);
                    annotationInstances.addAll(PrivacyPracticesHolder.getInstance(getProject())
                            .getManuallyAddedAnnotationsByType(CoconutAnnotationType.DataAccess));
                    Set<String> usedDataGroupStrings = annotationInstances.stream().map(
                            instance -> instance.getAnnotationHolder().getDataType()).collect(Collectors.toSet());
                    Set<PersonalDataGroup> unusedDataGroups = Arrays.stream(PersonalDataGroup.values()).filter(
                            v -> !usedDataGroupStrings.contains(v.toString())).collect(Collectors.toSet());
                    Set<String> unusedDataCategories = unusedDataGroups.stream().map(
                            d -> CoconutUIUtil.prettifyDataCategoryString(d.toString())).collect(Collectors.toSet());
                    Map<PersonalDataGroup, Boolean> dataGroupOccurrenceCount = new HashMap<>();
                    for (String category : unusedDataCategories) {
                        for (PersonalDataGroup dataGroup : unusedDataGroups) {
                            if (category.equals(CoconutUIUtil.prettifyDataCategoryString(dataGroup.toString()))) {
                                dataGroupOccurrenceCount.put(dataGroup,
                                        searchKeywords(CoconutUIUtil.getKeywordRegex(dataGroup)));
                            }
                        }
                    }
                    childrenNodes.add(new AnnotationActionNode(myProject, 2,
                            DataPracticeGroup.MANUALLY_ADDED_SOURCE, myBuilder, true, unusedDataCategories,
                            unusedDataGroups, dataGroupOccurrenceCount));
                    childrenNodes.add(new AnnotationActionNode(myProject, 3,
                            DataPracticeGroup.MANUALLY_ADDED_SOURCE, myBuilder, false));
                } else {
                    boolean hasMatch = searchKeywords(NetworkKeywordRegex);
                    childrenNodes.add(new AnnotationActionNode(myProject, 2,
                            DataPracticeGroup.MANUALLY_ADDED_SINK, myBuilder, true, hasMatch));
                    childrenNodes.add(new AnnotationActionNode(myProject, 3,
                            DataPracticeGroup.MANUALLY_ADDED_SINK, myBuilder, false));
                }
            }
        } else {
            ArrayList<String> unverifiedLibs = new ArrayList<>();
            ArrayList<String> verifiedLibs = new ArrayList<>();
            ArrayList<String> defaultLibs = new ArrayList<>();
            CoconutUIUtil.updateLibraryMaps(myProject, libraryDataMap);

            ApplicationManager.getApplication().invokeLater(
                    () -> ApplicationManager.getApplication().runWriteAction(
                            (ThrowableComputable<Boolean, RuntimeException>) () ->
                                    CoconutUIUtil.checkLibraryConfigFileCompletenessAndUpdate(
                                            myProject, libraryDataMap, true)));

            for (String libName : libraryDataMap.keySet()) {
                ThirdPartyCustomDataInstance customDataInstance =
                        PrivacyPracticesHolder.getInstance(myProject).getThirdPartyCustomDataInstance(libName);
                if (customDataInstance == null) {
                    defaultLibs.add(libName);
                } else if (customDataInstance.verified) {
                    verifiedLibs.add(libName);
                } else {
                    unverifiedLibs.add(libName);
                }
            }

            childrenNodes.add(new LibNode(myProject, unverifiedLibs, myBuilder, VerifyCustomLibText));
            childrenNodes.add(new LibNode(myProject, verifiedLibs, myBuilder, VerifiedCustomLibText));
            childrenNodes.add(new LibNode(myProject, defaultLibs, myBuilder, DefaultLibText));
        }
        return childrenNodes;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {

    }
}
