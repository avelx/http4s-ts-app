export interface Todo {
  id: number;
  title: string;
  done: boolean;
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });
  if (!res.ok) throw new Error(`${init?.method ?? "GET"} ${url} failed: ${res.status}`);
  return res.status === 204 ? (undefined as T) : res.json();
}

export const api = {
  hello: (name: string) =>
    request<{ message: string }>(`/api/hello?name=${encodeURIComponent(name)}`),
  listTodos: () => request<Todo[]>("/api/todos"),
  addTodo: (title: string) =>
    request<Todo>("/api/todos", { method: "POST", body: JSON.stringify({ title }) }),
  toggleTodo: (id: number) => request<Todo>(`/api/todos/${id}/toggle`, { method: "POST" }),
  deleteTodo: (id: number) => request<void>(`/api/todos/${id}`, { method: "DELETE" }),
};
