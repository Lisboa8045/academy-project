export type NotificationType =
  'APPOINTMENT_CONFIRMED' |
  'APPOINTMENT_CANCELLED' |
  'APPOINTMENT_REVIEW_ADDED' |
  'SERVICE_ON_SALE'

export interface NotificationModel {
  id: number,
  title: string,
  body: string,
  url: string,
  seen: boolean,
  notificationType: NotificationType;
}
