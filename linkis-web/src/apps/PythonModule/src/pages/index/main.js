import { LetgoPageBase } from '../../letgo/pageBase';

export class Main extends LetgoPageBase {
  constructor (ctx) {
    super(ctx);
    this.pythonModuleName = '';
    this.userName = '';
    this.engineType = '';
    this.isExpired = 0;
    this.isLoaded = null;
    this.currentPage = 1;
    this.pageSize = 10;
    this.totalPages = 0;
    this.totalRecords = 0;
    this.pythonModuleList = [];
    this.selectedModule = {};
    this.newModuleName = '';
    this.selectedEngineType = 'spark';
    this.selectedModuleDescription = '';
    this.selectedModulePath = '';
    this.selectedModuleIsLoad = 1;
    this.selectedModuleIsExpire = 0;
    this.selectedModuleId = null;
    this.selectedModuleFile = null;
    this.selectedModuleFileError = '';
    this.selectedModuleFileUploadStatus = false;
    this.addPythonModuleVisible = false;
    this.editPythonModuleVisible = false;
    this.deleteConfirmationVisible = false;
    this.loadStatusChangeConfirmationVisible = false;
    this.addModuleModalVisible = false;
    this.editModuleModalVisible = false;
    this.addFormRef = null;
    this.editFormRef = null;
    this.tutorialVisible = false;
  }

  onMounted () {
    if (!localStorage.getItem('hasRead')) {
      this.showTutorial();
    }
    this.loadPythonModuleList();
  }

  async loadPythonModuleList () {
    try {
      const params = {
        name: this.pythonModuleName,
        engineType: this.engineType,
        username: this.userName,
        // isLoad: this.isLoaded,
        isExpire: this.isExpired,
        pageNow: this.currentPage,
        pageSize: this.pageSize
      };
      if (this.isLoaded === 0 || this.isLoaded === 1) {
        params.isLoad = this.isLoaded;
      }
      const response =
                await this.$pageCode.apiPythonlistUdf.trigger(params);
      this.pythonModuleList = response.data.pythonList;
      this.totalRecords = response.data.totalPage;
      return response;
    } catch (error) {
      window.console.error(error);
      // throw error;
    }
  }

  handlePageChange (currentPage, pageSize) {
    this.currentPage = currentPage;
    this.loadPythonModuleList();
  }

  resetQueryParameters () {
    this.pythonModuleName = '';
    this.userName = '';
    this.engineType = '';
    this.isExpired = 0;
    this.isLoaded = null;
    this.currentPage = 1;
    this.pageSize = 10;
    this.loadPythonModuleList();
  }

  showAddModuleModal () {
    this.addPythonModuleVisible = true;
    this.selectedModule.name = '';
    this.selectedModule.engineType = 'spark';
    this.selectedModule.isExpire = 0;
    this.selectedModule.isLoad = 1;
    this.selectedModule.path = '';
    this.selectedModule.fileList = [];
    this.selectedModule.description = '';
  }

  showTutorial () {
    this.tutorialVisible = true;
  }

  showEditModuleModal (selectedModule) {
    if (selectedModule && typeof selectedModule === 'object') {
      this.selectedModule = {
        ...selectedModule
      };
      this.selectedModule.fileList = [{ uid: '12345', name: '123' }];
      this.editPythonModuleVisible = true;
    } else {
      this.$utils.FMessage.error({
        content: 'Invalid Module'
      });
    }
  }

  showDeleteConfirmation (selectedModule) {
    this.selectedModule = selectedModule;
    this.$utils.FModal.confirm({
      title: this.$pageCode.$t('confirmDelete'),
      content: this.$pageCode.$t('confirmDeleteContent', { name: selectedModule.name }),
      okText: this.$pageCode.$t('confirm'),
      cancelText: this.$pageCode.$t('cancel'),
      mask: false,
      onOk: async () => {
        try {
          await this.expirePythonModule(
            selectedModule.id,
            selectedModule.isExpire
          );
          this.$utils.FMessage.success({
            content: this.$pageCode.$t('deleteSuccess')
          });
          await this.loadPythonModuleList();
        } catch (error) {
          // this.$utils.FMessage.error({
          //     content: '删除模块时发生错误，请检查网络或稍后重试。'
          // });
          window.console.error(error);
        }
      },
      onCancel: () => {
        this.deleteConfirmationVisible = false;
      }
    });
    this.deleteConfirmationVisible = true;
  }

  showLoadStatusChangeConfirmation (selectedModule) {
    this.selectedModule = { ...selectedModule };
    this.loadStatusChangeConfirmationVisible = true;
    this.$utils.FModal.confirm({
      title: this.$pageCode.$t('confirmStatusChange'),
      mask: false,
      okText: this.$pageCode.$t('confirm'),
      cancelText: this.$pageCode.$t('cancel'),
      content: this.$pageCode.$t('confirmStatusChangeContent', { name: selectedModule.name }),
      onOk: async () => {
        await this.handleLoadStatusChange();
        this.loadStatusChangeConfirmationVisible = false;
      },
      onCancel: () => {
        this.loadStatusChangeConfirmationVisible = false;
      }
    });
  }

  async validateModuleName (newModuleName) {
    if (newModuleName.split('.')[0].length > 50) {
      this.$utils.FMessage.error(this.$pageCode.$t('moduleNameTooLong'));
      throw new Error(this.$pageCode.$t('moduleNameTooLong'));
    }
    // 名称只支持数字字母下划线，且以字母开头
    if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(newModuleName.split('.')[0])) {
      this.$utils.FMessage.error(this.$pageCode.$t('moduleNameNotFormat'));
      throw new Error(this.$pageCode.$t('moduleNameNotFormat'));
    }
    try {
      const response = await this.$pageCode.apiPythonfileexistUdf.trigger(
        {
          fileName: newModuleName
        }
      );
      if (response.status === 0) {
        return response.data;
      } else {
        this.$utils.FMessage.error(this.$pageCode.$t('moduleNameExist', { name: newModuleName }));
        throw new Error(response.message);
      }
    } catch (err) {
      // this.$utils.FMessage.error(`模块名称${newModuleName}已存在，如需重新上传请先删除旧的模块`);
      throw new Error(this.$pageCode.$t('moduleNameExist', { name: newModuleName }));
    }
  }

  async validateModuleSize (size) {
    if (size > 52428800) {
      this.$utils.FMessage.error(this.$pageCode.$t('moduleSizeExceed'));
      throw new Error(this.$pageCode.$t('moduleSizeExceed'));
    }
  }

  async validateModuleFile (file) {
    try {
      await this.validateModuleSize(file.size);
      await this.validateModuleName(file.name);
      this.selectedModule.fileList = this.selectedModule.fileList.filter(item => item.uid !== file.uid);
    } catch (err) {
      window.console.error(err);
      return false;
    }
  }

  async handleUploadHttpRequest (options) {
    try {
      const formData = new FormData();
      window.console.log('options:', options);
      formData.append('file', options.file);
      formData.append('fileName', options.file.name);
      const response = await this.$pageCode.apiPythonuploadFilesystem.trigger(formData);
      this.selectedModule.path = response.data.filePath;
      this.selectedModule.name = options.file.name.split('.')[0];
      this.selectedModule.fileList = [options.file];
    } catch (err) {
      window.console.error(err);
      // this.$utils.FMessage.error('上传失败');
    }
  }

  async savePythonModule (
    newModuleName,
    selectedEngineType,
    selectedModuleDescription,
    selectedModulePath,
    selectedModuleIsLoad,
    selectedModuleIsExpire,
    selectedModuleId
  ) {
    const params = {
      name: newModuleName,
      description: selectedModuleDescription,
      path: selectedModulePath,
      engineType: selectedEngineType,
      isLoad: selectedModuleIsLoad,
      isExpire: selectedModuleIsExpire
    };
    if (selectedModuleId) {
      params.id = selectedModuleId;
    }
    try {
      const response =
                await this.$pageCode.apiPythonsaveUdf.trigger(params);
      if (response.status === 0) {
        await this.$utils.FMessage.success({
          content: this.$pageCode.$t('saveSuccess')
        });
      } else {
        await this.$utils.FMessage.error({
          content: response.message
        });
      }
      return response;
    } catch (error) {
      // await this.$utils.FMessage.error({
      //     content: '保存失败，请检查网络或稍后重试。'
      // });
      window.console.error(error);
      throw error;
    }
  }

  async expirePythonModule (selectedModuleId, isExpired) {
    const response = await this.$pageCode.apiPythondeleteUdf.trigger({
      id: selectedModuleId,
      isExpire: isExpired
    });
    return response;
  }

  async handleAddModule () {
    await this.addFormRef.validate();
    try {
      await this.savePythonModule(
        this.selectedModule.name,
        this.selectedModule.engineType,
        this.selectedModule.description,
        this.selectedModule.path,
        this.selectedModule.isLoad,
        this.selectedModule.isExpire
      );
      this.addPythonModuleVisible = false;
      this.loadPythonModuleList();
    } catch (err) {
      window.console.error(err);
    }
  }

  async handleEditModule () {
    await this.editFormRef.validate();
    try {
      await this.savePythonModule(
        this.selectedModule.name,
        this.selectedModule.engineType,
        this.selectedModule.description,
        this.selectedModule.path,
        this.selectedModule.isLoad,
        this.selectedModule.isExpire,
        this.selectedModule.id
      );
      this.closeEditModuleModal();
      this.loadPythonModuleList();
    } catch (err) {
      window.console.error(err);
    }
  }

  async handleDeleteModule () {
    try {
      if (this.selectedModuleId === null) {
        throw new Error('No Module ID');
      }
      const response = await this.expirePythonModule(
        this.selectedModuleId
      );
      if (response.status === 0) {
        this.$utils.FMessage.success({
          content: response.message
        });
      } else {
        this.$utils.FMessage.error({
          content: response.message
        });
      }
    } catch (error) {
      // this.$utils.FMessage.error({
      //     content: '模块删除失败，请检查网络或稍后重试。'
      // });
      window.console.error('Error during delete module:', error);
    }
  }

  async handleLoadStatusChange () {
    const { id, name, path, isExpire, isLoad, engineType, description } =
            this.selectedModule;
    window.console.log({
      id,
      name,
      path,
      isExpire,
      isLoad,
      engineType,
      description
    });
    const targetLoadStatus = isLoad === 1 ? 0 : 1;
    if (id === null) {
      this.$utils.FMessage.error({
        content: 'No Module ID'
      });
      return;
    }
    try {
      await this.savePythonModule(
        name,
        engineType,
        description,
        path,
        targetLoadStatus,
        isExpire,
        id
      );
      await this.loadPythonModuleList();
    } catch (error) {
      // this.$utils.FMessage.error({
      //     content: '模块加载状态更新时发生错误：' + error.message
      // });
      window.console.error(error);
    }
  }

  closeAddModuleModal () {
    this.addPythonModuleVisible = false;
  }

  handleFileListChange ({ file, fileList }) {
    this.selectedModule.fileList = [];
  }

  closeEditModuleModal () {
    this.editPythonModuleVisible = false;
  }

  closeDeleteConfirmation () {
    this.deleteConfirmationVisible = false;
  }

  closeLoadStatusChangeConfirmation () {
    this.loadStatusChangeConfirmationVisible = false;
  }
}