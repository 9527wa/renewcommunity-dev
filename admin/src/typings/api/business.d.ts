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
      description?: string;
    }

    /** complaint status: 0 pending, 1 processing, 2 resolved */
    type ComplaintStatus = 0 | 1 | 2;

    interface Complaint {
      id: number;
      title: string;
      content: string;
      community: string;
      type: string;
      status: ComplaintStatus;
      projectId: number | null;
      projectName: string | null;
      contactName: string;
      contactMobile: string;
      reply: string;
      handledAt: string | null;
      createdAt: string;
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
      type?: string;
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
  }
}
