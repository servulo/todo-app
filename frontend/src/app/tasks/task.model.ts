export type TaskStatus = 'CRIADA' | 'ANDAMENTO' | 'CONCLUIDA';
export type TaskPriority = 'BAIXA' | 'MEDIA' | 'ALTA';

export interface Task {
  id: string;
  title: string;
  description: string | null;
  priority: TaskPriority;
  status: TaskStatus;
  dueDate: string | null;
  createdAt: string;
  completedAt: string | null;
}

export interface CreateTaskRequest {
  title: string;
  description?: string | null;
  priority?: TaskPriority;
  dueDate?: string | null;
}

export interface UpdateTaskRequest {
  title: string;
  description?: string | null;
  priority: TaskPriority;
  dueDate?: string | null;
}

export interface UpdateStatusRequest {
  status: TaskStatus;
}
