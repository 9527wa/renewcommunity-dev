import axios from 'axios';
import { getServiceBaseURL } from '@/utils/service';
import { getToken, TOKEN_HEADER } from '../request/shared';
import { request } from '../request';
/** get dashboard statistics */
export function fetchGetDashboard() {
  return request<Api.Business.Dashboard>({ url: '/api/system/dashboard' });
}

/** get project list */
export function fetchGetProjectList(params: Api.Business.ProjectSearchParams) {
  return request<Api.Business.ProjectList>({
    url: '/api/project/list',
    method: 'get',
    params
  });
}

/** get all projects for selects */
export function fetchGetProjectOptions() {
  return request<Api.Business.Project[]>({ url: '/api/project/options' });
}

/** create project */
export function fetchCreateProject(data: Api.Business.ProjectSaveParams) {
  return request<number>({ url: '/api/project', method: 'post', data });
}

/** update project */
export function fetchUpdateProject(id: number, data: Api.Business.ProjectSaveParams) {
  return request<void>({ url: `/api/project/${id}`, method: 'put', data });
}

/** delete project */
export function fetchDeleteProject(id: number) {
  return request<void>({ url: `/api/project/${id}`, method: 'delete' });
}

/** get complaint list */
export function fetchGetComplaintList(params: Api.Business.ComplaintSearchParams) {
  return request<Api.Business.ComplaintList>({
    url: '/api/complaint/list',
    method: 'get',
    params
  });
}

/** create complaint */
export function fetchCreateComplaint(data: Api.Business.ComplaintSaveParams) {
  return request<number>({ url: '/api/complaint', method: 'post', data });
}

/** update complaint */
export function fetchUpdateComplaint(id: number, data: Api.Business.ComplaintSaveParams) {
  return request<void>({ url: `/api/complaint/${id}`, method: 'put', data });
}

/** handle complaint */
export function fetchHandleComplaint(id: number, data: Api.Business.ComplaintHandleParams) {
  return request<void>({ url: `/api/complaint/${id}/handle`, method: 'put', data });
}

/** delete complaint */
export function fetchDeleteComplaint(id: number) {
  return request<void>({ url: `/api/complaint/${id}`, method: 'delete' });
}

/** get fund list */
export function fetchGetFundList(params: Api.Business.FundSearchParams) {
  return request<Api.Business.FundList>({
    url: '/api/fund/list',
    method: 'get',
    params
  });
}

/** create fund record */
export function fetchCreateFund(data: Api.Business.FundSaveParams) {
  return request<number>({ url: '/api/fund', method: 'post', data });
}

/** update fund record */
export function fetchUpdateFund(id: number, data: Api.Business.FundSaveParams) {
  return request<void>({ url: `/api/fund/${id}`, method: 'put', data });
}

/** delete fund record */
export function fetchDeleteFund(id: number) {
  return request<void>({ url: `/api/fund/${id}`, method: 'delete' });
}

/** get community list */
export function fetchGetCommunityList(params: Api.Business.CommunitySearchParams) {
  return request<Api.Business.CommunityList>({
    url: '/api/community/list',
    method: 'get',
    params
  });
}

/** get all enabled communities for selects */
export function fetchGetCommunityOptions() {
  return request<Api.Business.Community[]>({ url: '/api/community/options' });
}

/** create community */
export function fetchCreateCommunity(data: Api.Business.CommunitySaveParams) {
  return request<number>({ url: '/api/community', method: 'post', data });
}

/** update community */
export function fetchUpdateCommunity(id: number, data: Api.Business.CommunitySaveParams) {
  return request<void>({ url: `/api/community/${id}`, method: 'put', data });
}

/** delete community */
export function fetchDeleteCommunity(id: number) {
  return request<void>({ url: `/api/community/${id}`, method: 'delete' });
}

/** get announcement list */
export function fetchGetAnnouncementList(params: Api.Business.AnnouncementSearchParams) {
  return request<Api.Business.AnnouncementList>({
    url: '/api/announcement/list',
    method: 'get',
    params
  });
}

/** create announcement */
export function fetchCreateAnnouncement(data: Api.Business.AnnouncementSaveParams) {
  return request<number>({ url: '/api/announcement', method: 'post', data });
}

/** update announcement */
export function fetchUpdateAnnouncement(id: number, data: Api.Business.AnnouncementSaveParams) {
  return request<void>({ url: `/api/announcement/${id}`, method: 'put', data });
}

/** publish announcement */
export function fetchPublishAnnouncement(id: number) {
  return request<void>({ url: `/api/announcement/${id}/publish`, method: 'put' });
}

/** take announcement offline */
export function fetchOfflineAnnouncement(id: number) {
  return request<void>({ url: `/api/announcement/${id}/offline`, method: 'put' });
}

/** delete announcement */
export function fetchDeleteAnnouncement(id: number) {
  return request<void>({ url: `/api/announcement/${id}`, method: 'delete' });
}

/** get document list */
export function fetchGetDocumentList(params: Api.Business.DocumentSearchParams) {
  return request<Api.Business.DocumentList>({
    url: '/api/document/list',
    method: 'get',
    params
  });
}

/** upload document */
export function fetchUploadDocument(data: FormData) {
  return request<number>({ url: '/api/document', method: 'post', data });
}

/** update document status: 0 normal, 1 archived */
export function fetchUpdateDocumentStatus(id: number, status: Api.Business.DocumentStatus) {
  return request<void>({ url: `/api/document/${id}/status`, method: 'put', data: { status } });
}

/** delete document */
export function fetchDeleteDocument(id: number) {
  return request<void>({ url: `/api/document/${id}`, method: 'delete' });
}

/** download document file with token header */
export async function downloadDocument(id: number, fileName?: string | null) {
  const isHttpProxy = import.meta.env.DEV && import.meta.env.VITE_HTTP_PROXY === 'Y';
  const { baseURL } = getServiceBaseURL(import.meta.env, isHttpProxy);
  const response = await axios.get(`${baseURL}/api/document/${id}/download`, {
    responseType: 'blob',
    headers: { [TOKEN_HEADER]: getToken() }
  });

  const blobUrl = window.URL.createObjectURL(response.data as Blob);
  const link = document.createElement('a');
  link.href = blobUrl;
  link.download = fileName || 'download';
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(blobUrl);
}

/** get progress list */
export function fetchGetProgressList(params: Api.Business.ProgressSearchParams) {
  return request<Api.Business.ProgressList>({
    url: '/api/progress/list',
    method: 'get',
    params
  });
}

/** create progress record with site images */
export function fetchCreateProgress(data: FormData) {
  return request<number>({ url: '/api/progress', method: 'post', data });
}

/** update progress metadata */
export function fetchUpdateProgress(id: number, data: Api.Business.ProgressUpdateParams) {
  return request<void>({ url: `/api/progress/${id}`, method: 'put', data });
}

/** append site images to progress */
export function fetchAddProgressImages(id: number, data: FormData) {
  return request<void>({ url: `/api/progress/${id}/images`, method: 'post', data });
}

/** delete progress record */
export function fetchDeleteProgress(id: number) {
  return request<void>({ url: `/api/progress/${id}`, method: 'delete' });
}

/** get acceptance list */
export function fetchGetAcceptanceList(params: Api.Business.AcceptanceSearchParams) {
  return request<Api.Business.AcceptanceList>({
    url: '/api/acceptance/list',
    method: 'get',
    params
  });
}

/** create acceptance record with attachments */
export function fetchCreateAcceptance(data: FormData) {
  return request<number>({ url: '/api/acceptance', method: 'post', data });
}

/** update acceptance metadata */
export function fetchUpdateAcceptance(id: number, data: Api.Business.AcceptanceUpdateParams) {
  return request<void>({ url: `/api/acceptance/${id}`, method: 'put', data });
}

/** delete acceptance record */
export function fetchDeleteAcceptance(id: number) {
  return request<void>({ url: `/api/acceptance/${id}`, method: 'delete' });
}

/** fetch a stored file as a blob object url (for preview) */
export async function fetchFileObjectUrl(fileId: number) {
  const blob = await fetchFileBlob(fileId);

  return window.URL.createObjectURL(blob);
}

/** download a stored file */
export async function downloadSystemFile(fileId: number, fileName?: string | null) {
  const blob = await fetchFileBlob(fileId);
  const blobUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = blobUrl;
  link.download = fileName || 'download';
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(blobUrl);
}

async function fetchFileBlob(fileId: number) {
  const isHttpProxy = import.meta.env.DEV && import.meta.env.VITE_HTTP_PROXY === 'Y';
  const { baseURL } = getServiceBaseURL(import.meta.env, isHttpProxy);
  const response = await axios.get(`${baseURL}/api/file/${fileId}/download`, {
    responseType: 'blob',
    headers: { [TOKEN_HEADER]: getToken() }
  });

  return response.data as Blob;
}

/** get satisfaction list */
export function fetchGetSatisfactionList(params: Api.Business.SatisfactionSearchParams) {
  return request<Api.Business.SatisfactionList>({
    url: '/api/satisfaction/list',
    method: 'get',
    params
  });
}

/** get satisfaction stats */
export function fetchGetSatisfactionStats() {
  return request<Api.Business.SatisfactionStats>({ url: '/api/satisfaction/stats' });
}

/** get resident list */
export function fetchGetResidentList(params: Api.Business.ResidentSearchParams) {
  return request<Api.Business.ResidentList>({
    url: '/api/resident/list',
    method: 'get',
    params
  });
}

/** bind resident to community */
export function fetchBindResidentCommunity(id: number, communityId: number) {
  return request<void>({ url: `/api/resident/${id}/community`, method: 'put', data: { communityId } });
}

/** get resident binding history */
export function fetchGetResidentBindings(id: number) {
  return request<Api.Business.CommunityBinding[]>({ url: `/api/resident/${id}/bindings` });
}

/** get statistics overview */
export function fetchGetStatisticsOverview() {
  return request<Api.Business.Statistics>({ url: '/api/statistics/overview' });
}

/** get stored file list */
export function fetchGetFileList(params: Api.Business.FileSearchParams) {
  return request<Api.Business.FileList>({
    url: '/api/file/list',
    method: 'get',
    params
  });
}

/** delete stored file */
export function fetchDeleteFileRecord(id: number) {
  return request<void>({ url: `/api/file/${id}`, method: 'delete' });
}

/** get operation log list */
export function fetchGetOperationLogList(params: Api.Business.OperationLogSearchParams) {
  return request<Api.Business.OperationLogList>({
    url: '/api/system/log/list',
    method: 'get',
    params
  });
}
