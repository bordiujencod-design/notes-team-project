# GitHub Flow Steps

## Pentru fiecare student

1. Cloneaza repository-ul:

```bash
git clone <repo-url>
cd notes-team-project
```

2. Creeaza un branch nou:

```bash
git checkout -b nume-student-sectiune
```

Exemple:

```bash
git checkout -b daniel-project-skeleton
git checkout -b vlad-wireframes
git checkout -b alexandra-documentation
```

3. Modifica fisierele pentru partea ta.

4. Verifica modificarile:

```bash
git status
```

5. Adauga fisierele modificate:

```bash
git add .
```

6. Fa un commit cu mesaj clar:

```bash
git commit -m "Add project skeleton"
```

7. Trimite branch-ul pe GitHub:

```bash
git push origin daniel-project-skeleton
```

8. Deschide un Pull Request pe GitHub.

9. Cere review de la un coleg.

10. Dupa review, fa merge in `main`.

11. Actualizeaza local branch-ul `main`:

```bash
git checkout main
git pull origin main
```

## Pull Request model

```md
## What changed

- Added project skeleton
- Added initial README sections
- Added basic notes app files

## How to test

- Open index.html in browser
- Add a note
- Sort notes
- Delete a note
```

