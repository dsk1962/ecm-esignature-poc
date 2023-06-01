package com.davita.ecm.esign.model.extension.workflow;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Workflow {
	private String name;
	private String id;
	private String displayName;
	private Date created;
	private Date modified;
	private String status;
	private String scope;
	private String description;
	private String originatorId;
	private AgreementNameInfo agreementNameInfo;
	private MessageInfo messageInfo;
	private List<FileInfo> fileInfos;
	private List<RecipientsListInfo> recipientsListInfo;
	private List<CcsListInfo> ccsListInfo;
	private PasswordInfo passwordInfo;
	private List<MergeFieldsInfo> mergeFieldsInfo;
}
