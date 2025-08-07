export type NotificationType = 'ADDED_TO_CART' | 'REMOVED_FROM_CART' | 'APPOINTMENT_CONFIRMED' | 'APPOINTMENT_REJECTED' | 'APPOINTMENT_CANCELLED' | 'APPOINTMENT_RESCHEDULED' | 'PAYMENT_SUCCESS' | 'PAYMENT_FAILED' | 'WARNING';

export interface NotificationModel {
  id: number,
  title: string,
  body: string,
  url: string,
  seen: boolean,
  createdAt: string,
  notificationType: NotificationType;
}
