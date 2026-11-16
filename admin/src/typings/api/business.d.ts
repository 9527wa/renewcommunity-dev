/**
 * Namespace Api
 *
 * Backend api type of the business modules
 */
declare namespace Api {
  namespace Business {
    /** project status: 0 planning, 1 ongoing, 2 completed */
    type ProjectStatus = 0 | 1 | 2;

    interface Project {
      id: number;
      name: string;
      community: string;
      category: string;
      status: ProjectStatus;
      progress: number;
      budget: number;
      startDate: string | null;
      endDate: string | null;
      manager: string;
      constructionUnit: string;
      description: string;
    }

    interface ProjectList {
      total: number;
      list: Project[];
    }

    interface ProjectSearchParams {
      keyword?: string;
      status?: ProjectStatus | null;
      pageNum: number;
      pageSize: number;
    }

    interface ProjectSaveParams {
      name: string;
      community?: string;
      category?: string;
      status: ProjectStatus;
      progress: number;
      budget: number;
      startDate: string | null;
      endDate: string | null;
      manager?: string;
      constructionUnit?: string;
      description?: string;
    }

    /** complaint status: 0 pending, 1 processing, 2 resolved */
    type ComplaintStatus = 0 | 1 | 2;

    interface FileRefItem {
      fileId: number;
      fileName?: string | null;
      contentType?: string | null;
    }

    interface Complaint {
      id: number;
      title: string;
      content: string;
      community: string;
      location: string;
      type: string;
      kind: string;
      status: ComplaintStatus;
      projectId: number | null;
      projectName: string | null;
      contactName: string;
      contactMobile: string;
      reply: string;
      handledAt: string | null;
      createdAt: string;
      images: FileRefItem[];
    }

    interface ComplaintList {
      total: number;
      list: Complaint[];
    }

    interface ComplaintSearchParams {
      keyword?: string;
      status?: ComplaintStatus | null;
      pageNum: number;
      pageSize: number;
    }

    interface ComplaintSaveParams {
      title: string;
      content?: string;
      community?: string;
      location?: string;
      type?: string;
      kind?: string;
      projectId?: number | null;
      contactName?: string;
      contactMobile?: string;
    }

    interface ComplaintHandleParams {
      status: 1 | 2;
      reply?: string;
    }

    /** fund type: 1 allocation, 2 expenditure */
    type FundType = 1 | 2;

    interface Fund {
      id: number;
      projectId: number | null;
      projectName: string | null;
      name: string;
      type: FundType;
      amount: number;
      occurredAt: string;
      remark: string;
    }

    interface FundList {
      total: number;
      list: Fund[];
    }

    interface FundSearchParams {
      keyword?: string;
      type?: FundType | null;
      projectId?: number | null;
      pageNum: number;
      pageSize: number;
    }

    interface FundSaveParams {
      projectId?: number | null;
      name: string;
      type: FundType;
      amount: number;
      occurredAt: string;
      remark?: string;
    }

    /** dashboard statistics */
    interface Dashboard {
      project: {
        total: number;
        planning: number;
        ongoing: number;
        completed: number;
        averageProgress: number;
      };
      complaint: {
        total: number;
        pending: number;
        processing: number;
        resolved: number;
        thisMonth: number;
      };
      fund: {
        budget: number;
        allocated: number;
        spent: number;
        executionRate: number;
      };
      recentProjects: ProjectBrief[];
      recentComplaints: ComplaintBrief[];
    }

    interface ProjectBrief {
      id: number;
      name: string;
      community: string;
      status: ProjectStatus;
      progress: number;
      budget: number;
    }

    interface ComplaintBrief {
      id: number;
      title: string;
      community: string;
      status: ComplaintStatus;
      createdAt: string;
    }

    /** community */
    interface Community {
      id: number;
      name: string;
      address: string;
      builtYear: number | null;
      buildingCount: number;
      householdCount: number;
      manager: string;
      managerMobile: string;
      status: 0 | 1;
      remark: string;
    }

    interface CommunityList {
      total: number;
      list: Community[];
    }

    interface CommunitySearchParams {
      keyword?: string;
      status?: 0 | 1 | null;
      pageNum: number;
      pageSize: number;
    }

    interface CommunitySaveParams {
      name: string;
      address?: string;
      builtYear?: number | null;
      buildingCount?: number;
      householdCount?: number;
      manager?: string;
      managerMobile?: string;
      status: 0 | 1;
      remark?: string;
    }

    /** announcement status: 0 draft, 1 published, 2 offline */
    type AnnouncementStatus = 0 | 1 | 2;

    interface Announcement {
      id: number;
      title: string;
      content: string;
      type: string;
      status: AnnouncementStatus;
      publishedAt: string | null;
      createdAt: string;
    }

    interface AnnouncementList {
      total: number;
      list: Announcement[];
    }

    interface AnnouncementSearchParams {
      keyword?: string;
      status?: AnnouncementStatus | null;
      pageNum: number;
      pageSize: number;
    }

    interface AnnouncementSaveParams {
      title: string;
      content: string;
      type?: string;
    }

    /** document status: 0 normal, 1 archived */
    type DocumentStatus = 0 | 1;

    interface Document {
      id: number;
      projectId: number | null;
      projectName?: string | null;
      category: string;
      title: string;
      fileName?: string | null;
      fileSize?: number | null;
      contentType?: string | null;
      uploaderName?: string | null;
      status: DocumentStatus;
      createdAt: string;
    }

    interface DocumentList {
      total: number;
      list: Document[];
    }

    interface DocumentSearchParams {
      keyword?: string;
      category?: string | null;
      projectId?: number | null;
      status?: DocumentStatus | null;
      pageNum: number;
      pageSize: number;
    }

    /** progress image */
    interface ProgressImage {
      id: number;
      fileId: number;
      fileName?: string | null;
      fileSize?: number | null;
      contentType?: string | null;
    }

    /** project progress record */
    interface Progress {
      id: number;
      projectId: number;
      projectName?: string | null;
      stage: string;
      title: string;
      content: string;
      progress: number;
      reportDate: string;
      creatorName: string;
      createdAt: string;
      images: ProgressImage[];
    }

    interface ProgressList {
      total: number;
      list: Progress[];
    }

    interface ProgressSearchParams {
      projectId?: number | null;
      stage?: string | null;
      pageNum: number;
      pageSize: number;
    }

    interface ProgressUpdateParams {
      stage: string;
      title: string;
      content?: string;
      progress: number;
      reportDate: string;
    }

    /** acceptance result: 0 pending, 1 passed, 2 failed */
    type AcceptanceResult = 0 | 1 | 2;

    interface AcceptanceFile {
      id: number;
      fileId: number;
      fileName?: string | null;
      fileSize?: number | null;
      contentType?: string | null;
    }

    interface Acceptance {
      id: number;
      projectId: number;
      projectName?: string | null;
      title: string;
      result: AcceptanceResult;
      acceptDate: string | null;
      conclusion: string;
      creatorName: string;
      createdAt: string;
      attachments: AcceptanceFile[];
    }

    interface AcceptanceList {
      total: number;
      list: Acceptance[];
    }

    interface AcceptanceSearchParams {
      projectId?: number | null;
      result?: AcceptanceResult | null;
      pageNum: number;
      pageSize: number;
    }

    interface AcceptanceUpdateParams {
      title: string;
      result: AcceptanceResult;
      acceptDate: string | null;
      conclusion?: string;
    }

    /** satisfaction */
    interface Satisfaction {
      id: number;
      complaintId: number;
      complaintTitle?: string | null;
      userName: string;
      score: number;
      comment: string;
      createdAt: string;
    }

    interface SatisfactionList {
      total: number;
      list: Satisfaction[];
    }

    interface SatisfactionSearchParams {
      score?: number | null;
      pageNum: number;
      pageSize: number;
    }

    interface SatisfactionStats {
      total: number;
      averageScore: number;
      fiveCount: number;
      fourCount: number;
      goodRate: number;
      oneCount: number;
      twoCount: number;
      threeCount: number;
    }

    /** resident */
    interface Resident {
      id: number;
      username: string;
      nickname: string;
      mobile: string;
      email: string;
      status: 0 | 1;
      communityId: number | null;
      communityName?: string | null;
      createdAt: string;
    }

    interface ResidentList {
      total: number;
      list: Resident[];
    }

    interface ResidentSearchParams {
      keyword?: string;
      communityId?: number | null;
      pageNum: number;
      pageSize: number;
    }

    interface CommunityBinding {
      id: number;
      communityId: number;
      communityName?: string | null;
      operatorName: string;
      createdAt: string;
    }

    /** statistics */
    interface NameCount {
      name: string;
      count: number;
    }

    interface Statistics {
      project: {
        total: number;
        planning: number;
        ongoing: number;
        completed: number;
        averageProgress: number;
        categories: NameCount[];
      };
      complaint: {
        total: number;
        pending: number;
        processing: number;
        resolved: number;
        thisMonth: number;
        types: NameCount[];
      };
      fund: {
        budget: number;
        allocated: number;
        spent: number;
        executionRate: number;
      };
      document: {
        total: number;
        archived: number;
        categories: NameCount[];
      };
      satisfaction: {
        total: number;
        averageScore: number;
        goodRate: number;
      };
      progress: {
        total: number;
        thisMonth: number;
      };
    }

    /** file management */
    interface FileItem {
      id: number;
      originalName: string;
      contentType: string;
      size: number;
      uploaderName: string;
      createdAt: string;
    }

    interface FileList {
      total: number;
      list: FileItem[];
    }

    interface FileSearchParams {
      keyword?: string;
      pageNum: number;
      pageSize: number;
    }

    /** operation log */
    interface OperationLog {
      id: number;
      userId?: number | null;
      userName: string;
      method: string;
      path: string;
      ip: string;
      durationMs: number;
      success: 0 | 1;
      errorMsg: string;
      createdAt: string;
    }

    interface OperationLogList {
      total: number;
      list: OperationLog[];
    }

    interface OperationLogSearchParams {
      keyword?: string;
      success?: 0 | 1 | null;
      pageNum: number;
      pageSize: number;
    }
  }
}
