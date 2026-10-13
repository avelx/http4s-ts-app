import { api, Todo } from "./api.js";

const $ = <T extends HTMLElement>(sel: string) => document.querySelector<T>(sel)!;

const greeting = $<HTMLParagraphElement>("#greeting");
const form = $<HTMLFormElement>("#todo-form");
const input = $<HTMLInputElement>("#todo-title");
const list = $<HTMLUListElement>("#todo-list");

function renderTodo(todo: Todo): HTMLLIElement {
  const li = document.createElement("li");
  li.className = todo.done ? "done" : "";

  const label = document.createElement("span");
  label.textContent = todo.title;
  label.onclick = async () => {
    await api.toggleTodo(todo.id);
    await refresh();
  };

  const del = document.createElement("button");
  del.textContent = "✕";
  del.onclick = async () => {
    await api.deleteTodo(todo.id);
    await refresh();
  };

  li.append(label, del);
  return li;
}

async function refresh(): Promise<void> {
  const todos = await api.listTodos();
  list.replaceChildren(...todos.map(renderTodo));
}

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  const title = input.value.trim();
  if (!title) return;
  await api.addTodo(title);
  input.value = "";
  await refresh();
});

(async () => {
  greeting.textContent = (await api.hello("TypeScript")).message;
  await refresh();
})();
