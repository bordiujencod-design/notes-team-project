const form = document.querySelector("#noteForm");
const titleInput = document.querySelector("#titleInput");
const bodyInput = document.querySelector("#bodyInput");
const sortSelect = document.querySelector("#sortSelect");
const notesList = document.querySelector("#notesList");

const storageKey = "notes-team-project";

let notes = JSON.parse(localStorage.getItem(storageKey) || "[]");

function saveNotes() {
  localStorage.setItem(storageKey, JSON.stringify(notes));
}

function formatDate(value) {
  return new Intl.DateTimeFormat("ro-RO", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function sortedNotes() {
  const selectedSort = sortSelect.value;
  const copy = [...notes];

  if (selectedSort === "oldest") {
    return copy.sort((a, b) => a.createdAt - b.createdAt);
  }

  if (selectedSort === "title") {
    return copy.sort((a, b) => a.title.localeCompare(b.title, "ro"));
  }

  return copy.sort((a, b) => b.createdAt - a.createdAt);
}

function renderNotes() {
  const orderedNotes = sortedNotes();

  if (orderedNotes.length === 0) {
    notesList.innerHTML = '<div class="empty-state">Nu exista notite inca.</div>';
    return;
  }

  notesList.innerHTML = orderedNotes
    .map(
      (note) => `
        <article class="note-card">
          <header>
            <div>
              <h3>${escapeHtml(note.title)}</h3>
              <span class="note-date">${formatDate(note.createdAt)}</span>
            </div>
            <button class="delete-button" type="button" data-id="${note.id}">Sterge</button>
          </header>
          <p>${escapeHtml(note.body)}</p>
        </article>
      `,
    )
    .join("");
}

function escapeHtml(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

form.addEventListener("submit", (event) => {
  event.preventDefault();

  notes.push({
    id: crypto.randomUUID(),
    title: titleInput.value.trim(),
    body: bodyInput.value.trim(),
    createdAt: Date.now(),
  });

  saveNotes();
  form.reset();
  titleInput.focus();
  renderNotes();
});

sortSelect.addEventListener("change", renderNotes);

notesList.addEventListener("click", (event) => {
  const button = event.target.closest(".delete-button");

  if (!button) {
    return;
  }

  notes = notes.filter((note) => note.id !== button.dataset.id);
  saveNotes();
  renderNotes();
});

renderNotes();

