import { request } from '@/utils/request';

export interface PageData<T> {
  total: number;
  list: T[];
}

export interface AppAnnouncement {
  id: number;
  title: string;
  type: string;
  summary: string;
  publishedAt: string | null;
}

export interface AppAnnouncementDetail {
  id: number;
  title: string;
  content: string;
  type: string;
  publishedAt: string | null;
}

export interface AppProject {
  id: number;
  name: string;
  community: string;
  category: string;
  status: 0 | 1 | 2;
  progress: number;
  startDate: string | null;
  endDate: string | null;
  description: string;
}

export interface AppCommunity {
  id: number;
  name: string;
  address: string;
  buildingCount: number;
  householdCount: number;
}

export interface AppComplaint {
  id: number;
  title: string;
  content: string;
  type: string;
  status: 0 | 1 | 2;
  projectId: number | null;
  projectName?: string | null;
  reply?: string | null;
  createdAt: string;
  handledAt?: string | null;
}

export interface AppComplaintSubmitParams {
  title: string;
  content: string;
  type?: string;
  projectId?: number | null;
  community?: string;
  contactName?: string;
  contactMobile?: string;
}

/** 已发布公告列表 */
export function fetchAnnouncementList(params: { pageNum: number; pageSize: number }) {
  return request<PageData<AppAnnouncement>>({ url: '/app/announcement/list', data: params });
}

/** 公告详情 */
export function fetchAnnouncementDetail(id: number) {
  return request<AppAnnouncementDetail>({ url: `/app/announcement/${id}` });
}

/** 改造项目列表 */
export function fetchProjectList(params: { status?: number; community?: string; pageNum: number; pageSize: number }) {
  return request<PageData<AppProject>>({ url: '/app/project/list', data: params });
}

/** 项目详情 */
export function fetchProjectDetail(id: number) {
  return request<AppProject>({ url: `/app/project/${id}` });
}

/** 小区列表 */
export function fetchCommunityList() {
  return request<AppCommunity[]>({ url: '/app/community/list' });
}

/** 提交投诉 */
export function submitComplaint(data: AppComplaintSubmitParams) {
  return request<number>({ url: '/app/complaint', method: 'POST', data });
}

/** 我的投诉 */
export function fetchMyComplaints(params: { pageNum: number; pageSize: number }) {
  return request<PageData<AppComplaint>>({ url: '/app/complaint/my', data: params });
}
