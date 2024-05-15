import { TrashIcon } from "@navikt/aksel-icons";
import {
  Box,
  Button,
  HGrid,
  HStack,
  TextField,
  Textarea,
  UNSAFE_Combobox,
  VStack,
} from "@navikt/ds-react";
import { useForm } from "@tanstack/react-form";
import { useNavigate } from "@tanstack/react-router";
import { useEffect } from "react";
import { Locale, LocaleValues, Tekst, localeString } from "src/model";
import { useMutateText } from "src/queries";

type Props =
  | {
      text: Tekst;
      isNew?: undefined;
    }
  | {
      isNew: true;
      text?: undefined;
    };

const TextEditor = ({ text, isNew }: Props) => {
  const mutateText = useMutateText();
  const navigate = useNavigate();

  const form = useForm({
    defaultValues: {
      overskrift: text?.overskrift ?? "",
      tags: text?.tags ?? [],
      innhold: (text?.innhold
        ? Object.entries(text.innhold).map(([locale, content]) => ({
            locale,
            content,
          }))
        : [{ locale: "nb_NO", content: "" }]) as {
        locale: keyof typeof Locale;
        content: string;
      }[],
    },
    onSubmit: async ({ value }) => {
      const sumitValue = {
        id: text?.id,
        ...value,
        innhold: value.innhold.reduce(
          (t, value) => ({
            ...t,
            [value.locale]: value.content,
          }),
          {} as Tekst["innhold"],
        ),
      };
      const res = await mutateText.mutateAsync(sumitValue);
      if (res.id && isNew) {
        navigate({ to: `/tekster/${res.id}` });
      }
      form.reset();
    },
  });

  useEffect(() => {
    form.reset();
  }, [text?.id, form]);
  return (
    <Box>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          e.stopPropagation();
          form.handleSubmit();
        }}
      >
        <VStack gap="4">
          {!isNew && (
            <HStack gap="4">
              <TextField label="Tekst-id" value={text.id} readOnly />
              <TextField label="Vekttall" value={text.vekttall} readOnly />
            </HStack>
          )}
          <form.Field
            name="overskrift"
            validators={{
              onChange: ({ value }) =>
                !value ? "Overskrift er påkrevd" : undefined,
            }}
            children={(field) => (
              <TextField
                label="Overskrift"
                name={field.name}
                value={field.state.value}
                onBlur={field.handleBlur}
                onChange={(e) => field.handleChange(e.currentTarget.value)}
                error={field.state.meta.errors[0]}
              />
            )}
          />
          <form.Field
            name="tags"
            children={(field) => (
              <UNSAFE_Combobox
                label="Tags"
                name={field.name}
                isMultiSelect
                allowNewValues
                options={field.state.value}
                onToggleSelected={(option, isSelected) => {
                  isSelected
                    ? field.handleChange((prev) => [...prev, option])
                    : field.handleChange((prev) =>
                        prev.filter((o) => o !== option),
                      );
                }}
                selectedOptions={field.state.value}
              />
            )}
          />
          <form.Field
            name="innhold"
            mode="array"
            children={(field) => (
              <>
                {field.state.value.map(({ locale }, i) => (
                  <form.Field key={locale} name={`innhold[${i}].content`}>
                    {(subfield) => (
                      <HStack gap="2">
                        <Box className="grow">
                          <Textarea
                            resize="vertical"
                            minRows={5}
                            label={localeString[locale]}
                            value={subfield.state.value}
                            onChange={(e) =>
                              subfield.handleChange(e.currentTarget.value)
                            }
                          />
                        </Box>
                        <Box style={{ marginTop: "2em" }}>
                          <Button
                            type="button"
                            variant="danger"
                            icon={<TrashIcon />}
                            onClick={() => field.removeValue(i)}
                          >
                            Slett
                          </Button>
                        </Box>
                      </HStack>
                    )}
                  </form.Field>
                ))}
                <HGrid gap="4" columns={4}>
                  {LocaleValues.filter(
                    (v) => !field.state.value.map((l) => l.locale).includes(v),
                  ).map((locale) => (
                    <Button
                      type="button"
                      variant="secondary"
                      onClick={() => {
                        field.pushValue({ locale, content: "" });
                      }}
                    >{`Legg til ${localeString[locale]}`}</Button>
                  ))}
                </HGrid>
              </>
            )}
          />

          <form.Subscribe
            selector={(state) => [
              state.canSubmit,
              state.isSubmitting,
              state.isTouched,
            ]}
            children={([canSubmit, isSubmitting, isTouched]) => (
              <Button
                type="submit"
                disabled={!canSubmit || !isTouched}
                variant="primary"
                loading={isSubmitting}
              >
                Lagre
              </Button>
            )}
          />
        </VStack>
      </form>
    </Box>
  );
};

export default TextEditor;
